package com.cloudbees.jenkins.plugins.filter.pullrequest;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class PullRequestCreatedActionFilter extends PullRequestActionFilter {

    @DataBoundConstructor
    public PullRequestCreatedActionFilter() {
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Created"; }
    }
}
