package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.BitbucketEvent;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public interface BitbucketEventTriggerMatcher {
    boolean matchesAction(BitbucketEvent bitbucketEvent, BitbucketTriggerFilter triggerFilter);
}
