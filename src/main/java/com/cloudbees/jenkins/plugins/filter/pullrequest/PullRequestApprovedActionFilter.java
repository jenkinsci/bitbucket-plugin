package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.pullrequest.PullRequestApprovedCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by isvillar on 14/03/2016.
 */
public class PullRequestApprovedActionFilter extends PullRequestActionFilter {
    public boolean triggerOnlyIfAllReviewersApproved;

    @DataBoundConstructor
    public PullRequestApprovedActionFilter(boolean triggerOnlyIfAllReviewersApproved) {
        this.triggerOnlyIfAllReviewersApproved = triggerOnlyIfAllReviewersApproved;
    }

    @Override
    public boolean shouldTriggerBuild(BitBucketPayload bitbucketPayload) {
        if(triggerOnlyIfAllReviewersApproved) {
            if(!allReviewersHaveApproved(bitbucketPayload)) {
                LOGGER.info("Not triggered because not all reviewers have approved the pull request");
                return false;
            }
        }

        return true;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload pullRequestPayload) throws IOException {
        return new PullRequestApprovedCause(pollingLog, pullRequestPayload);
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Approved"; }
    }


    public boolean getTriggerOnlyIfAllReviewersApproved() {
        return triggerOnlyIfAllReviewersApproved;
    }

    private boolean allReviewersHaveApproved(BitBucketPayload pullRequestPayload) {
        JSONObject pullRequestJSON = pullRequestPayload.getPayload().getJSONObject("pullrequest");
        JSONArray participants = pullRequestJSON.getJSONArray("participants");

        boolean allApproved = true;

        for(int i = 0; i < participants.size(); i++) {
            if(!participants.getJSONObject(i).getBoolean("approved")) {
                allApproved = false;
            }
        }

        return allApproved;
    }

    private static final Logger LOGGER = Logger.getLogger(PullRequestApprovedActionFilter.class.getName());
}
