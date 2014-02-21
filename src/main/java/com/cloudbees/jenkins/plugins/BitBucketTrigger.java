package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.git.GitStatus;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.DataBoundConstructor;


/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketTrigger extends Trigger<AbstractProject> {

    @DataBoundConstructor
    public BitBucketTrigger() {
    }

    public void onPost(String user, URIish repository, String sha1, String branch) {
        for (GitStatus.Listener listener : Jenkins.getInstance().getExtensionList(GitStatus.Listener.class)) {
            listener.onNotifyCommit(repository, sha1, new String[] { branch });
        }
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to BitBucket";
        }
    }
}
