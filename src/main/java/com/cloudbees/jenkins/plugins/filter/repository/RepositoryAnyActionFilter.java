package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.repository.RepositoryCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * Created by isvillar on 15/03/2016.
 */
public class RepositoryAnyActionFilter extends RepositoryActionFilter {
    @DataBoundConstructor
    public RepositoryAnyActionFilter() {
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
        public String getDisplayName() { return "Any"; }
    }
}
