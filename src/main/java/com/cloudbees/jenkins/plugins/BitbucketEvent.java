package com.cloudbees.jenkins.plugins;

/**
 * Bitbucket events
 * @since August 1, 2016
 * @version 2.0
 */
public class BitbucketEvent {
    public interface EVENT {
        String REPOSITORY = "repo";
        String PULL_REQUEST = "pullrequest";
    }

    public interface REPOSITORY_ACTIONS {
        String PUSH = "push";
    }

    public interface PULL_REQUEST_ACTIONS {
        String CREATED = "created";
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

    /**
     * Returns {@code true} if the action is defined
     *
     * @return {@code true} if the action is defined
     */
    private boolean checkAction(String action) {
        if(EVENT.REPOSITORY.equals(name)) {
            if (REPOSITORY_ACTIONS.PUSH.equals(action)) {
                return true;
            }
        } else if(EVENT.PULL_REQUEST.equals(name)) {
            if(PULL_REQUEST_ACTIONS.APPROVED.equals(action)) {
                return true;
            } else if(PULL_REQUEST_ACTIONS.CREATED.equals(action)) {
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

    /**
     * Returns {@code true} if the event is defined
     *
     * @return {@code true} if the event is defined
     */
    private boolean checkValidEvent(String name) {
        if(EVENT.REPOSITORY.equals(name)
            || EVENT.PULL_REQUEST.equals(name)) {
            return true;
        }

        return false;
    }
}
