package com.cloudbees.jenkins.plugins.cause.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;

import java.io.File;
import java.io.IOException;

/**
 * Created by isvillar on 15/03/2016.
 */
public class PullRequestCause extends BitbucketTriggerCause {

    public PullRequestCause(File pollingLog, BitBucketPayload bitBucketPayload) throws IOException {
        super(pollingLog, bitBucketPayload);
    }

    @Override
    public String getShortDescription() {
        String pusher = bitBucketPayload.getUser() != null ? bitBucketPayload.getUser() : "";
        return "Started by Bitbucket pull request event by " + pusher;
    }
}
