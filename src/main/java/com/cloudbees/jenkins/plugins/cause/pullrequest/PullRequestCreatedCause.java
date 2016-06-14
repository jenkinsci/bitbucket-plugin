package com.cloudbees.jenkins.plugins.cause.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;

import java.io.File;
import java.io.IOException;

/**
 * Created by Douglas Miller on 2016/06/13.
 */
public class PullRequestCreatedCause extends BitbucketTriggerCause {
    public PullRequestCreatedCause(File pollingLog, BitbucketPayload bitbucketPayload) throws IOException {
        super(pollingLog, bitbucketPayload);
    }

    @Override
    public String getShortDescription() {
        return "Started by Bitbucket pull request creation";
    }

}
