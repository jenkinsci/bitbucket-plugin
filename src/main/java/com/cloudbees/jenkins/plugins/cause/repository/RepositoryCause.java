package com.cloudbees.jenkins.plugins.cause.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;

import java.io.File;
import java.io.IOException;

/**
 * Created by isvillar on 15/03/2016.
 */
public class RepositoryCause extends BitbucketTriggerCause {
    public RepositoryCause(File pollingLog, BitBucketPayload bitBucketPayload) throws IOException {
        super(pollingLog, bitBucketPayload);
    }

    @Override
    public String getShortDescription() {
        String pusher = bitBucketPayload.getUser() != null ? bitBucketPayload.getUser() : "";
        return "Started by Bitbucket repository event by " + pusher;
    }
}
