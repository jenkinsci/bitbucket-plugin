package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.model.AbstractDescribableImpl;

import java.io.File;
import java.io.IOException;

/**
 * The base {@link AbstractDescribableImpl} class for {@link @PullRequestActionFilter}
 * @since August 1, 2016
 * @version 2.0
 */
public abstract class PullRequestActionFilter extends AbstractDescribableImpl<PullRequestActionFilter> {
    public abstract boolean shouldTriggerBuild(BitbucketPayload bitbucketPayload);
    public abstract BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException;
}
