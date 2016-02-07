package com.cloudbees.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketTrigger extends Trigger<Job<?, ?>> {

	@DataBoundConstructor
	public BitBucketTrigger() {
	}

	/**
	 * Called when a POST is made.
	 */
	@Deprecated
	public void onPost(String triggeredByUser) {
		onPost(triggeredByUser, "");
	}

	/**
	 * Called when a POST is made.
	 */
	public void onPost(String triggeredByUser, final String payload) {
		final String pushBy = triggeredByUser;
		SequentialExecutionQueue queue = new SequentialExecutionQueue(Hudson.MasterComputer.threadPoolForRemoting);

		queue.execute(new Runnable() {
			@Override
			public void run() {
				String name = " #" + job.getNextBuildNumber();
				BitBucketPushCause cause;
				try {
					cause = new BitBucketPushCause(getLogFile(), pushBy);
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Failed to parse the polling log", e);
					cause = new BitBucketPushCause(pushBy);
				}
				ParameterizedJobMixIn pJob = new ParameterizedJobMixIn() {
					@Override
					protected Job asJob() {
						return job;
					}
				};
				BitBucketPayload bitBucketPayload = new BitBucketPayload(payload);
				pJob.scheduleBuild2(5, new CauseAction(cause), bitBucketPayload);
				if (pJob.scheduleBuild(cause)) {
					LOGGER.info("SCM changes detected in " + job.getName() + ". Triggering " + name);
				} else {
					LOGGER.info("SCM changes detected in " + job.getName() + ". Job is already in the queue");
				}
			}
		});
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		return Collections.singleton(new BitBucketWebHookPollingAction());
	}

	/**
	 * Returns the file that records the last/current polling activity.
	 */
	public File getLogFile() {
		return new File(job.getRootDir(), "bitbucket-polling.log");
	}

	/**
	 * Check if "bitbucket-polling.log" already exists to initialize it
	 */
	public boolean IsLogFileInitialized() {
		File file = new File(job.getRootDir(), "bitbucket-polling.log");
		return file.exists();
	}

	/**
	 * Action object for {@link Project}. Used to display the polling log.
	 */
	public final class BitBucketWebHookPollingAction implements Action {
		public Job<?, ?> getOwner() {
			return job;
		}

		@Override
		public String getIconFileName() {
			return "clipboard.png";
		}

		@Override
		public String getDisplayName() {
			return "BitBucket Hook Log";
		}

		@Override
		public String getUrlName() {
			return "BitBucketPollLog";
		}

		public String getLog() throws IOException {
			return Util.loadFile(getLogFile());
		}

		/**
		 * Writes the annotated log to the given output.
		 */
		public void writeLogTo(XMLOutput out) throws IOException {
			new AnnotatedLargeText<BitBucketWebHookPollingAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
		}
	}

	@Extension
	public static class DescriptorImpl extends TriggerDescriptor {

		@Override
		public boolean isApplicable(Item item) {
			return item instanceof Job && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null && item instanceof ParameterizedJobMixIn.ParameterizedJob;
		}

		@Override
		public String getDisplayName() {
			return "Build when a change is pushed to BitBucket";
		}
	}

	private static final Logger LOGGER = Logger.getLogger(BitBucketTrigger.class.getName());
}
