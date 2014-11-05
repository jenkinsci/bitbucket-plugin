package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.StreamTaskListener;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketTrigger extends Trigger<AbstractProject> {

    @DataBoundConstructor
    public BitBucketTrigger() {
    }
    
    /**
     * Called when a POST is made.
     */
    public void onPost(String pushBy) {
        run(pushBy);
    }

    private boolean runPolling() {
        try {
            StreamTaskListener listener = new StreamTaskListener(getLogFile());
            try {
                PrintStream logger = listener.getLogger();
                long start = System.currentTimeMillis();
                logger.println("Started on "+ DateFormat.getDateTimeInstance().format(new Date()));
                boolean result = job.poll(listener).hasChanges();
                logger.println("Done. Took "+ Util.getTimeSpanString(System.currentTimeMillis()-start));
                if(result)
                    logger.println("Changes found");
                else
                    logger.println("No changes");
                return result;
            } catch (Error e) {
                e.printStackTrace(listener.error("Failed to record SCM polling"));
                LOGGER.log(Level.SEVERE,"Failed to record SCM polling",e);
                throw e;
            } catch (RuntimeException e) {
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
    
    public void run(String pushBy) {
        if (runPolling()) {
            String name = " #"+job.getNextBuildNumber();
            BitBucketPushCause cause;
            try {
                cause = new BitBucketPushCause(getLogFile(), pushBy);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to parse the polling log",e);
                cause = new BitBucketPushCause(pushBy);
            }
            if (job.scheduleBuild(cause)) {
                LOGGER.info("SCM changes detected in "+ job.getName()+". Triggering "+name);
            } else {
                LOGGER.info("SCM changes detected in "+ job.getName()+". Job is already in the queue");
            }
        }
    }

    /**
     * Returns the file that records the last/current polling activity.
     */
    public File getLogFile() {
        return new File(job.getRootDir(),"bitbucket-polling.log");
    }
    
    /**
     * Action object for {@link Project}. Used to display the polling log.
     */
    public final class BitBucketWebHookPollingAction implements Action {
        public AbstractProject<?,?> getOwner() {
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
        
        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to BitBucket";
        }
    }
    private static final Logger LOGGER = Logger.getLogger(BitBucketTrigger.class.getName());
}
