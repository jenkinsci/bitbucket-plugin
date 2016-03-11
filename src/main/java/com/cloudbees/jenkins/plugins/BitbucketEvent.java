package com.cloudbees.jenkins.plugins;

/**
 * Created by isvillar on 11/03/2016.
 */
public class BitbucketEvent {
    public interface EVENTS{
        String REPOSITORY = "repository";
        String PULL_REQUEST = "pullrequest";
        String ISSUE = "issue";
    }

    interface ACTIONS {

    }

    private String key;
    private String action;

    public BitbucketEvent(String requestAction) {
        String[] keyValuepair = requestAction.split(":");
        key = keyValuepair[0];
        action = keyValuepair[1];
    }

    public String getKey() {
        return key;
    }

    public String getAction() {
        return action;
    }
}
