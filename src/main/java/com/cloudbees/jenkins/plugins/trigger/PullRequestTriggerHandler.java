package com.cloudbees.jenkins.plugins.trigger;

import com.cloudbees.jenkins.plugins.BitBucketPushCause;
import com.cloudbees.jenkins.plugins.BitbucketPollingRunnable;
import com.cloudbees.jenkins.plugins.config.PullRequestTriggerConfig;
import com.cloudbees.jenkins.plugins.payload.PullRequestPayload;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.scm.PollingResult;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class PullRequestTriggerHandler {
    private PullRequestTriggerConfig pullRequestTriggerConfig;
    private PullRequestPayload pullRequestPayload;
    private File logFile;

    public PullRequestTriggerHandler(PullRequestTriggerConfig pullRequestTriggerConfig,
                                     PullRequestPayload pullRequestPayload,
                                     File logFile) {
        this.pullRequestPayload = pullRequestPayload;
        this.pullRequestTriggerConfig = pullRequestTriggerConfig;
        this.logFile = logFile;
    }

    public BitBucketPushCause getCause() throws IOException {
        return new BitBucketPushCause(logFile, pullRequestPayload);
    }

    public boolean shouldScheduleJob() {
        if(pullRequestTriggerConfig.isTriggerOnlyIfAllReviewersApproved()) {
            boolean allReviewersApproved = allReviewersHaveApproved();
            if(!allReviewersApproved) {
                LOGGER.info("Not triggered because not all reviewers have approved the pull request");
                return false;
            }
        }

        return true;
    }

    private boolean allReviewersHaveApproved() {
        JSONObject pullRequestJSON = pullRequestPayload.getPayload().getJSONObject("pullrequest");
        JSONArray participants = pullRequestJSON.getJSONArray("participants");

        boolean allApproved = true;

        for(int i = 0; i < participants.size(); i++) {
            if(!participants.getJSONObject(i).getBoolean("approved")) {
                allApproved = false;
            }
        }

        return allApproved;
    }

    private static final Logger LOGGER = Logger.getLogger(PullRequestTriggerHandler.class.getName());
}
