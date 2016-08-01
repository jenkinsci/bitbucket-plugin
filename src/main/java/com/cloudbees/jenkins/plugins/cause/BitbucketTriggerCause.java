package com.cloudbees.jenkins.plugins.cause;

import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.triggers.SCMTrigger;

import java.io.File;
import java.io.IOException;

/**
 * The base {@link BitbucketTriggerCause} for {@link SCMTrigger.SCMTriggerCause}
 * @since August 1, 2016
 * @version 1.1.6
 */
public abstract class BitbucketTriggerCause extends SCMTrigger.SCMTriggerCause {
    protected BitbucketPayload bitbucketPayload;

    public BitbucketTriggerCause(File pollingLog, BitbucketPayload bitbucketPayload) throws IOException {
        super(pollingLog);
        this.bitbucketPayload = bitbucketPayload;
    }
}
