package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.model.AbstractDescribableImpl;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shyri Villar on 15/03/2016.
 */
public abstract class RepositoryActionFilter extends AbstractDescribableImpl<RepositoryActionFilter> {
    public abstract boolean shouldTriggerBuild(BitBucketPayload bitbucketPayload);
    public abstract BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload bitBucketPayload) throws IOException;
}
