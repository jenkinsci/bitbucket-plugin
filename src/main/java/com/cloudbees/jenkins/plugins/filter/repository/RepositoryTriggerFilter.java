package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilterDescriptor;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestActionDescriptor;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestActionFilter;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class RepositoryTriggerFilter extends BitbucketTriggerFilter {
    public RepositoryActionFilter actionFilter;

    @DataBoundConstructor
    public RepositoryTriggerFilter(RepositoryActionFilter actionFilter) {
        this.actionFilter = actionFilter;
    }

    @Override
    public boolean shouldScheduleJob(BitBucketPayload bitbucketPayload) {
        return actionFilter.shouldTriggerBuild(bitbucketPayload);
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload pullRequestPayload) throws IOException {
        return actionFilter.getCause(pollingLog, pullRequestPayload);
    }

    @Override
    public RepositoryActionFilter getActionFilter() {
        return actionFilter;
    }

    @Extension
    public static class FilterDescriptorImpl extends BitbucketTriggerFilterDescriptor {
        public String getDisplayName() { return "Repository"; }

        public List<RepositoryActionDescriptor> getActionDescriptors() {
            // you may want to filter this list of descriptors here, if you are being very fancy
            return Jenkins.getInstance().getDescriptorList(RepositoryActionFilter.class);
        }
    }
}
