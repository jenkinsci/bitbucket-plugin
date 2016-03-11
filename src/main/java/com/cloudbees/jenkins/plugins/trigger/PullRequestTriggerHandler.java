package com.cloudbees.jenkins.plugins.trigger;

import com.cloudbees.jenkins.plugins.BitBucketPushCause;
import com.cloudbees.jenkins.plugins.BitbucketPollingRunnable;
import com.cloudbees.jenkins.plugins.config.PullRequestTriggerConfig;
import com.cloudbees.jenkins.plugins.payload.PullRequestPayload;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.scm.PollingResult;
import jenkins.model.ParameterizedJobMixIn;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class PullRequestTriggerHandler {
    private PullRequestTriggerConfig pullRequestTriggerConfig;

    public PullRequestTriggerHandler(PullRequestTriggerConfig pullRequestTriggerConfig) {
        this.pullRequestTriggerConfig = pullRequestTriggerConfig;
    }

    public BitBucketPushCause getCause(File logFile, PullRequestPayload pullRequestPayload) throws IOException {
        return new BitBucketPushCause(logFile, pullRequestPayload);
    }

    public boolean shouldScheduleJob() {
        return true;
    }
}
