/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.pullrequest.PullRequestApprovedCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The filter for PullRequestApprovedAction
 * @since August 1, 2016
 * @version 2.0
 */
public class PullRequestApprovedActionFilter extends PullRequestActionFilter {
    public boolean triggerOnlyIfAllReviewersApproved;

    @DataBoundConstructor
    public PullRequestApprovedActionFilter(boolean triggerOnlyIfAllReviewersApproved) {
        this.triggerOnlyIfAllReviewersApproved = triggerOnlyIfAllReviewersApproved;
    }

    @Override
    public boolean shouldTriggerBuild(BitbucketPayload bitbucketPayload) {
        if(triggerOnlyIfAllReviewersApproved) {
            if(!allReviewersHaveApproved(bitbucketPayload)) {
                LOGGER.info("Not triggered because not all reviewers have approved the pull request");
                return false;
            }
        }

        return true;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException {
        return new PullRequestApprovedCause(pollingLog, pullRequestPayload);
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends PullRequestActionDescriptor {
        public String getDisplayName() { return "Approved"; }
    }


    public boolean getTriggerOnlyIfAllReviewersApproved() {
        return triggerOnlyIfAllReviewersApproved;
    }

    private boolean allReviewersHaveApproved(BitbucketPayload pullRequestPayload) {
        JSONObject pullRequestJSON = pullRequestPayload.getPayload().getJSONObject("pullrequest");
        JSONArray participants = pullRequestJSON.getJSONArray("participants");

        boolean allApproved = true;

        for(int i = 0; i < participants.size(); i++) {
            JSONObject participant = participants.getJSONObject(i);
            if(isReviewer(participant)) {
                if (!participant.getBoolean("approved")) {
                    allApproved = false;
                }
            }
        }

        return allApproved;
    }

    private boolean isReviewer(JSONObject pullRequestParticipant) {
        String role = pullRequestParticipant.getString("role");
        return "REVIEWER".equals(role);
    }

    private static final Logger LOGGER = Logger.getLogger(PullRequestApprovedActionFilter.class.getName());

}
