package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.model.AbstractDescribableImpl;

import java.io.File;
import java.io.IOException;

/**
 * The base {@link AbstractDescribableImpl} class for {@link BitbucketTriggerFilter} implementations
 * @since August 1, 2016
 * @version 2.0
 */
public abstract class BitbucketTriggerFilter extends AbstractDescribableImpl<BitbucketTriggerFilter> {
    /**
     * Returns {@code true} if a build should be triggered based on the payload received by Bitbucket
     *
     * @return {@code true} if a build should be triggered based on the payload received by Bitbucket
     */
    public abstract boolean shouldScheduleJob(BitbucketPayload bitbucketPayload);

    /**
     * Gets the cause of the trigger
     *
     * @return the {@link BitbucketTriggerCause}
     */
    public abstract BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException;

    /**
     * Gets the {@link AbstractDescribableImpl} of the ActionFilter
     *
     * @return the {@link AbstractDescribableImpl}
     */
    public abstract AbstractDescribableImpl getActionFilter();
}
