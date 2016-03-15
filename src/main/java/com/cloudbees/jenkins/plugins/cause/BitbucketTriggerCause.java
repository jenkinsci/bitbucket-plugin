package com.cloudbees.jenkins.plugins.cause;

import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import com.cloudbees.jenkins.plugins.payload.PullRequestPayload;
import hudson.triggers.SCMTrigger;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public abstract class BitbucketTriggerCause extends SCMTrigger.SCMTriggerCause {
    protected BitBucketPayload bitBucketPayload;

    public BitbucketTriggerCause(File pollingLog, BitBucketPayload bitBucketPayload) throws IOException {
        super(pollingLog);
        this.bitBucketPayload = bitBucketPayload;
    }
}
