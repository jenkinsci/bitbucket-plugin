package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerMatcher;
import com.cloudbees.jenkins.plugins.filter.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public class FilterMatcher {

    public List<BitbucketTriggerFilter> getMatchingFilters(BitbucketEvent bitbucketEvent,
                                                           List<BitbucketTriggerFilter> triggerFilterList) {
        List<BitbucketTriggerFilter> filteredList = null;

        if(triggerFilterList != null) {
            filteredList = new ArrayList<BitbucketTriggerFilter>();

            for(BitbucketTriggerFilter triggerFilter : triggerFilterList) {
                if(matchesEventAndAction(bitbucketEvent, triggerFilter)) {
                    filteredList.add(triggerFilter);
                }
            }
        }

        return filteredList;
    }

    private boolean matchesEventAndAction(BitbucketEvent bitbucketEvent,
                                                                BitbucketTriggerFilter triggerFilter) {
        if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName()) &&
                triggerFilter instanceof PullRequestTriggerFilter) {
            return new PullRequestTriggerMatcher().matchesAction(bitbucketEvent, triggerFilter);
        } else if(BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName()) &&
                triggerFilter instanceof com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter) {
            return new RepositoryTriggerMatcher().matchesAction(bitbucketEvent, triggerFilter);
        }

        return false;
    }
}
