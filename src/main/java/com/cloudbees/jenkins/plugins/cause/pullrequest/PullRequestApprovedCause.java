package com.cloudbees.jenkins.plugins.cause.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;

import java.io.File;
import java.io.IOException;

/**
 * Created by isvillar on 15/03/2016.
 */
public class PullRequestApprovedCause extends BitbucketTriggerCause {
    public PullRequestApprovedCause(File pollingLog, BitbucketPayload bitbucketPayload) throws IOException {
        super(pollingLog, bitbucketPayload);
    }

    @Override
    public String getShortDescription() {
        String pusher = bitbucketPayload.getUser() != null ? bitbucketPayload.getUser() : "";
        return "Started by Bitbucket pull request approved by " + pusher;
    }

}
