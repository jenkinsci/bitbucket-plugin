package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class PullRequestCreatedActionFilter extends PullRequestActionFilter {

    @DataBoundConstructor
    public PullRequestCreatedActionFilter() {
    }

    @Override
    public boolean shouldTriggerBuild(BitBucketPayload bitbucketPayload) {
        return false;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload pullRequestPayload) throws IOException {
        return null;
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Created"; }
    }
}
