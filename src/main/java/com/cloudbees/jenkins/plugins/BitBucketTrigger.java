package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;


/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketTrigger extends Trigger<Job<?, ?>> {

    private String overrideUrl;
    private Boolean buildOnCreatedBranch;

    @DataBoundConstructor
    public BitBucketTrigger() { }

    // notice that the name of the getter must exactly like the parameter
    public String getOverrideUrl() {
        return overrideUrl;
    }

    @DataBoundSetter
    public void setOverrideUrl(String overrideUrl){
        this.overrideUrl = overrideUrl;
    }

    public Boolean getBuildOnCreatedBranch() {
        return buildOnCreatedBranch;
    }

    @DataBoundSetter
    public void setBuildOnCreatedBranch(Boolean buildOnCreatedBranch) {
        this.buildOnCreatedBranch = buildOnCreatedBranch;
    }

    /**
     * Called when a POST is made.
     */
    public void onPost(String triggeredByUser, final String payload, String branchName) {
        getDescriptor().queue.execute(
                new BitBucketTriggerRunnable(
                        payload,
                        this.job,
                        LOGGER,
                        triggeredByUser,
                        branchName,
                        buildOnCreatedBranch
                )
        );
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
            start = new AnnotatedLargeText<>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(start, out.asWriter());
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
