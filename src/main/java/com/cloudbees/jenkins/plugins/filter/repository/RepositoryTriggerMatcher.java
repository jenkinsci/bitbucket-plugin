package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.filter.BitbucketEventTriggerMatcher;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;

/**
 * {link @code RepositoryTriggerMatcher} for {link @BitbucketEventTriggerMatcher}
 * @since August 1, 2016
 * @version 2.0
 */
public class RepositoryTriggerMatcher implements BitbucketEventTriggerMatcher {
    @Override
    public boolean matchesAction(BitbucketEvent bitbucketEvent, BitbucketTriggerFilter triggerFilter) {
        if(triggerFilter.getActionFilter() instanceof RepositoryAnyActionFilter) {
            return true;
        } else if(triggerFilter.getActionFilter() instanceof RepositoryPushActionFilter &&
                BitbucketEvent.REPOSITORY_ACTIONS.PUSH.equals(bitbucketEvent.getAction())) {
            return true;
        }
        return true;
    }
}
