package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.BitbucketEvent;

/**
 * Determinates if the event received by Bitbucket matches any action defined in the plugin
 * @since August 1, 2016
 * @version 2.0
 */
public interface BitbucketEventTriggerMatcher {
    /**
     * Returns {@code true} if the event received by Bitbucket matches any action defined in the plugin
     *
     * @return {@code true} if the event received by Bitbucket matches any action defined in the plugin
     */
    boolean matchesAction(BitbucketEvent bitbucketEvent, BitbucketTriggerFilter triggerFilter);
}
