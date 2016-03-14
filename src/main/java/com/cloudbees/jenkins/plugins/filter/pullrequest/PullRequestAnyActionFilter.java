package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilterDescriptor;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class PullRequestAnyActionFilter extends PullRequestActionFilter {
    @DataBoundConstructor
    public PullRequestAnyActionFilter() {
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Any"; }
    }
}
