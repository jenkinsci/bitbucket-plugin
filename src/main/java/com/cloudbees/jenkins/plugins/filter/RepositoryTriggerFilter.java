package com.cloudbees.jenkins.plugins.filter;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class RepositoryTriggerFilter extends BitbucketTriggerFilter{
    @DataBoundConstructor
    public RepositoryTriggerFilter() {
    }

    @Extension
    public static class FilterDescriptorImpl extends BitbucketTriggerFilterDescriptor {
        public String getDisplayName() { return "Repository"; }
    }
}
