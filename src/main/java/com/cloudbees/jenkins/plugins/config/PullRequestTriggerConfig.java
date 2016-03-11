package com.cloudbees.jenkins.plugins.config;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class PullRequestTriggerConfig {
    public boolean triggerOnlyIfAllReviewersApproved;
    @DataBoundConstructor

    public PullRequestTriggerConfig(boolean triggerOnlyIfAllReviewersApproved) {
        this.triggerOnlyIfAllReviewersApproved = triggerOnlyIfAllReviewersApproved;
    }

    public boolean isTriggerOnlyIfAllReviewersApproved() {
        return triggerOnlyIfAllReviewersApproved;
    }
}
