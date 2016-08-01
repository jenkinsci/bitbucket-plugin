package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.repository.RepositoryCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * The filter for RepositoryPushAction
 * @since August 1, 2016
 * @version 2.0
 */
public class RepositoryPushActionFilter extends RepositoryActionFilter {

    @DataBoundConstructor
    public RepositoryPushActionFilter() {
    }

    @Override
    public boolean shouldTriggerBuild(BitbucketPayload bitbucketPayload) {
        return true;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload bitbucketPayload) throws IOException {
        return new RepositoryCause(pollingLog, bitbucketPayload);
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends RepositoryActionDescriptor {
        public String getDisplayName() { return "Push"; }
    }
}
