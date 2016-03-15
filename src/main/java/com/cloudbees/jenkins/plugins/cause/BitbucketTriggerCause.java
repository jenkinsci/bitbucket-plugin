package com.cloudbees.jenkins.plugins.cause;

import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.triggers.SCMTrigger;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public abstract class BitbucketTriggerCause extends SCMTrigger.SCMTriggerCause {
    protected BitbucketPayload bitbucketPayload;

    public BitbucketTriggerCause(File pollingLog, BitbucketPayload bitbucketPayload) throws IOException {
        super(pollingLog);
        this.bitbucketPayload = bitbucketPayload;
    }
}
