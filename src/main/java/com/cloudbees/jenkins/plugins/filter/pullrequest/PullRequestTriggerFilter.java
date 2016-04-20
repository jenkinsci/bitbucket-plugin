package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilterDescriptor;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class PullRequestTriggerFilter extends BitbucketTriggerFilter {
    public PullRequestActionFilter actionFilter;

    @DataBoundConstructor
    public PullRequestTriggerFilter(PullRequestActionFilter actionFilter) {
        this.actionFilter = actionFilter;
    }


    @Override
    public boolean shouldScheduleJob(BitbucketPayload bitbucketPayload) {
        return actionFilter.shouldTriggerBuild(bitbucketPayload);
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException {
        return actionFilter.getCause(pollingLog, pullRequestPayload);
    }

    @Extension
    public static class FilterDescriptorImpl extends BitbucketTriggerFilterDescriptor {
        public String getDisplayName() { return "Pull Request"; }

        public List<PullRequestActionDescriptor> getActionDescriptors() {
            // you may want to filter this list of descriptors here, if you are being very fancy
            return Jenkins.getInstance().getDescriptorList(PullRequestActionFilter.class);
        }
    }

    public PullRequestActionFilter getActionFilter() {
        return actionFilter;
    }
}
