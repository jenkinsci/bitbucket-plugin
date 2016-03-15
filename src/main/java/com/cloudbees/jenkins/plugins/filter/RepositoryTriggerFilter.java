package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestActionFilter;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class RepositoryTriggerFilter extends BitbucketTriggerFilter{
    @DataBoundConstructor
    public RepositoryTriggerFilter() {
    }

    @Override
    public boolean shouldScheduleJob(BitBucketPayload bitbucketPayload) {
        return false;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload pullRequestPayload) throws IOException {
        return null;
    }

    @Override
    public PullRequestActionFilter getActionFilter() {
        return null;
    }

    @Extension
    public static class FilterDescriptorImpl extends BitbucketTriggerFilterDescriptor {
        public String getDisplayName() { return "Repository"; }
    }
}
