package com.cloudbees.jenkins.plugins.filter.pullrequest;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by isvillar on 14/03/2016.
 */
public class PullRequestApprovedActionFilter extends PullRequestActionFilter {
    @DataBoundConstructor
    public PullRequestApprovedActionFilter() {
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Approved"; }
    }
}
