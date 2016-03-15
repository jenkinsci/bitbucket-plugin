package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.pullrequest.PullRequestApprovedCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class PullRequestAnyActionFilter extends PullRequestActionFilter {
    public boolean triggerOnlyIfAllReviewersApproved;

    @DataBoundConstructor
    public PullRequestAnyActionFilter(boolean triggerOnlyIfAllReviewersApproved) {
        this.triggerOnlyIfAllReviewersApproved = triggerOnlyIfAllReviewersApproved;
    }

    @Override
    public boolean shouldTriggerBuild(BitbucketPayload bitbucketPayload) {
        return true;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException {
        return new PullRequestApprovedCause(pollingLog, pullRequestPayload);
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Any"; }
    }
}
