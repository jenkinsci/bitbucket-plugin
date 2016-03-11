package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.config.PullRequestTriggerConfig;
import com.cloudbees.jenkins.plugins.config.RepositoryTriggerConfig;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import hudson.scm.PollingResult;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketTrigger extends Trigger<Job<?, ?>> {
    public RepositoryTriggerConfig repositoryTriggerConfig;
    public PullRequestTriggerConfig pullRequestTriggerConfig;

    @DataBoundConstructor
    public BitBucketTrigger(RepositoryTriggerConfig repositoryTriggerConfig,
                            PullRequestTriggerConfig pullRequestTriggerConfig) {
        this.repositoryTriggerConfig = repositoryTriggerConfig;
        this.pullRequestTriggerConfig = pullRequestTriggerConfig;
    }

    /**
     * Called when a POST is made.
     */
    public void onPost(final BitbucketEvent bitbucketEvent, final BitBucketPayload bitBucketPayload) {
        if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName())) {
            if(pullRequestTriggerConfig != null) {
                BitbucketPollingRunnable bitbucketPollingRunnable = new BitbucketPollingRunnable(job,
                        getLogFile(),
                        new BitbucketPollingRunnable.BitbucketPollResultListener() {
                            @Override
                            public void onPollSuccess(PollingResult pollingResult) {
                                String name = " #"+job.getNextBuildNumber();

                                BitBucketPushCause cause;

                                try {
                                    cause = new BitBucketPushCause(getLogFile(), bitBucketPayload.getUser());
                                } catch (IOException e) {
                                    LOGGER.log(Level.WARNING, "Failed to parse the polling log",e);
                                    cause = new BitBucketPushCause(bitBucketPayload.getUser());
                                }

                                ParameterizedJobMixIn pJob = new ParameterizedJobMixIn() {
                                    @Override protected Job asJob() {
                                        return job;
                                    }
                                };

                                pJob.scheduleBuild2(5, new CauseAction(cause), bitBucketPayload);
                                if (pJob.scheduleBuild(cause)) {
                                    LOGGER.info("SCM changes detected in "+ job.getName()+". Triggering "+ name);
                                } else {
                                    LOGGER.info("SCM changes detected in "+ job.getName()+". Job is already in the queue");
                                }
                            }

                            @Override
                            public void onPollError(Throwable throwable) {

                            }
                        });

                getDescriptor().queue.execute(bitbucketPollingRunnable);
            } else {
                LOGGER.info(bitbucketEvent.getName() + " received but job " + job.getName() + " is not configured to handle it");
            }
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new BitBucketWebHookPollingAction());
    }

    /**
     * Returns the file that records the last/current polling activity.
     */
    public File getLogFile() {
        return new File(job.getRootDir(),"bitbucket-polling.log");
    }

    /**
     * Check if "bitbucket-polling.log" already exists to initialize it
     */
    public boolean IsLogFileInitialized() {
        File file = new File(job.getRootDir(),"bitbucket-polling.log");
        return file.exists();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Action object for {@link Project}. Used to display the polling log.
     */
    public final class BitBucketWebHookPollingAction implements Action {
        public Job<?,?> getOwner() {
            return job;
        }

        public String getIconFileName() {
            return "clipboard.png";
        }

        public String getDisplayName() {
            return "BitBucket Hook Log";
        }

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
            new AnnotatedLargeText<BitBucketWebHookPollingAction>(getLogFile(), Charset.defaultCharset(),true,this).writeHtmlTo(0,out.asWriter());
        }
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {
        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Hudson.MasterComputer.threadPoolForRemoting);

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof Job && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to BitBucket";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BitBucketTrigger.class.getName());
}
