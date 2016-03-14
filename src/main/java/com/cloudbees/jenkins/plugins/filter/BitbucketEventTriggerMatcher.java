package com.cloudbees.jenkins.plugins.filter;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public interface BitbucketEventTriggerMatcher {
    boolean matchesAction();
}
