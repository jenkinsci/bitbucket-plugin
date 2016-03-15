package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.filter.BitbucketEventTriggerMatcher;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;

/**
 * Created by Shyri Villar on 14/03/2016.
 *
 */
public class PullRequestTriggerMatcher implements BitbucketEventTriggerMatcher {
    @Override
    public boolean matchesAction(BitbucketEvent bitbucketEvent, BitbucketTriggerFilter triggerFilter) {
        if(triggerFilter.getActionFilter() instanceof PullRequestAnyActionFilter) {
            return true;
        } else {
            if(BitbucketEvent.PULL_REQUEST_ACTIONS.APPROVED.equals(bitbucketEvent.getAction()) &&
                    triggerFilter.getActionFilter() instanceof PullRequestApprovedActionFilter) {
                return true;
            }
        }
        return false;
    }
}
