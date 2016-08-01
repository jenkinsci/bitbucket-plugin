package com.cloudbees.jenkins.plugins.filter.repository;

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
 * The base {link @code BitbucketTriggerFilter} for repositories
 * @since August 1, 2016
 * @version 2.0
 */
public class RepositoryTriggerFilter extends BitbucketTriggerFilter {
    public RepositoryActionFilter actionFilter;

    @DataBoundConstructor
    public RepositoryTriggerFilter(RepositoryActionFilter actionFilter) {
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
