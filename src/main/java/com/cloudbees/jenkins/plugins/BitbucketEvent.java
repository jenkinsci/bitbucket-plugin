package com.cloudbees.jenkins.plugins;

/**
 * Created by isvillar on 11/03/2016.
 */
public class BitbucketEvent {
    public interface EVENT {
        String REPOSITORY = "repo";
        String PULL_REQUEST = "pullrequest";
    }

    interface REPOSITORY_ACTIONS {
        String PUSH = "push";
    }

    interface PULL_REQUEST_ACTIONS {
        String APPROVED = "approved";
    }

    private String name;
    private String action;

    public BitbucketEvent(String requestAction) {
        String[] keyValuepair = requestAction.split(":");
        String name = keyValuepair[0];
        String action = keyValuepair[1];

        if(!checkValidEvent(name)) {
            throw new UnsupportedOperationException(name + " event is not valid or unsupported");
        } else {
            this.name = name;
        }

        if(!checkAction(action)) {
            throw new UnsupportedOperationException(action + " action for " + name + " event " );
        } else {
            this.action = action;
        }
    }

    private boolean checkAction(String action) {
        if(EVENT.REPOSITORY.equals(name)) {
            if (REPOSITORY_ACTIONS.PUSH.equals(action)) {
                return true;
            }
        } else if(EVENT.PULL_REQUEST.equals(name)) {
            if(PULL_REQUEST_ACTIONS.APPROVED.equals(action)) {
                return true;
            }
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public String getAction() {
        return action;
    }

    private boolean checkValidEvent(String name) {
        if(EVENT.REPOSITORY.equals(name)
            || EVENT.PULL_REQUEST.equals(name)) {
            return true;
        }

        return false;
    }
}
