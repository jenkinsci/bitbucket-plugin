package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
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

    private final String overrideUrl;

    @DataBoundConstructor
    public BitBucketTrigger(String overrideUrl) {
        this.overrideUrl = overrideUrl;
    }

    // notice that the name of the getter must exactly like the parameter
    public String getOverrideUrl() {
        return overrideUrl;
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
        getDescriptor().queue.execute(new Runnable() {
            private boolean runPolling() {
                try {
                    StreamTaskListener listener = new StreamTaskListener(getLogFile());
                    try {
                        PrintStream logger = listener.getLogger();
                        long start = System.currentTimeMillis();
                        logger.println("Started on "+ DateFormat.getDateTimeInstance().format(new Date()));
                        SCMTriggerItem scmTriggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
                        if ( scmTriggerItem == null){
                            return false;
                        } else {
                            boolean result = scmTriggerItem.poll(listener).hasChanges();
                            logger.println("Done. Took "+ Util.getTimeSpanString(System.currentTimeMillis()-start));
                            if(result)
                                logger.println("Changes found");
                            else
                                logger.println("No changes");
                            return result;
                        }
                    } catch (Error | RuntimeException e) {
                        e.printStackTrace(listener.error("Failed to record SCM polling"));
                        LOGGER.log(Level.SEVERE,"Failed to record SCM polling",e);
                        throw e;
                    } finally {
                        listener.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,"Failed to record SCM polling",e);
                }
                return false;
            }

            public void run() {
                if (runPolling()) {
                    if ( job == null){
                        LOGGER.info("job is null");
                        return;
                    }
                    String name = " #"+job.getNextBuildNumber();
                    BitBucketPushCause cause;
                    try {
                        cause = new BitBucketPushCause(getLogFile(), pushBy);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to parse the polling log",e);
                        cause = new BitBucketPushCause(pushBy);
                    }
                    ParameterizedJobMixIn pJob = new ParameterizedJobMixIn() {
                        @Override protected Job asJob() {
                            return job;
                        }
                    };
                    BitBucketPayload bitBucketPayload = new BitBucketPayload(payload);
                    pJob.scheduleBuild2(5, new CauseAction(cause), bitBucketPayload);
                    if (pJob.scheduleBuild(cause)) {
                        if ( job != null) {
                            LOGGER.info("SCM changes detected in " + job.getName() + ". Triggering " + name);
                        }
                    } else {
                        if ( job != null) {
                            LOGGER.info("SCM changes detected in " + job.getName() + ". Job is already in the queue");
                        }
                    }
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
        if ( job == null){
            throw new RuntimeException("job is null");
        } else {
            return new File(job.getRootDir(),"bitbucket-polling.log");
        }

    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Action object. Used to display the polling log.
     */
    public final class BitBucketWebHookPollingAction implements Action {
        long start = 0;
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
            start = new AnnotatedLargeText<BitBucketWebHookPollingAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(start, out.asWriter());
        }
    }

    @Extension @Symbol("bitbucketPush")
    public static class DescriptorImpl extends TriggerDescriptor {
        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Hudson.MasterComputer.threadPoolForRemoting);

        // Must be inside the DescriptorImpl
        public FormValidation doCheckOverrideUrl(@QueryParameter String value) throws IOException, ServletException {
            return FormValidation.ok();
        }

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
