package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.filter.BitbucketEventTriggerMatcher;

/**
 * Created by Shyri Villar on 14/03/2016.
 *
 */
public class PullRequestTriggerMatcher implements BitbucketEventTriggerMatcher {

    @Override
    public boolean matchesAction() {
        return false;
    }
}
