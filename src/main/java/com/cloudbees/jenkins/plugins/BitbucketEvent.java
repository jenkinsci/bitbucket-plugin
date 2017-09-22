/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.jenkins.plugins;

/**
 * Bitbucket events
 *
 * @version 2.0
 * @since August 1, 2016
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
        String UPDATED = "updated";
    }

    private String name;
    private String action;

    public BitbucketEvent(String requestAction) {
        String[] keyValuepair = requestAction.split(":");
        String name = keyValuepair[0];
        String action = keyValuepair[1];

        if (!checkValidEvent(name)) {
            throw new UnsupportedOperationException(name + " event is not valid or unsupported");
        } else {
            this.name = name;
        }

        if (!checkAction(action)) {
            throw new UnsupportedOperationException(action + " action for " + name + " event ");
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
        if (EVENT.REPOSITORY.equals(name)) {
            if (REPOSITORY_ACTIONS.PUSH.equals(action)) {
                return true;
            }
        } else if (EVENT.PULL_REQUEST.equals(name)) {
            if (PULL_REQUEST_ACTIONS.APPROVED.equals(action)) {
                return true;
            } else if (PULL_REQUEST_ACTIONS.CREATED.equals(action)) {
                return true;
            } else if (PULL_REQUEST_ACTIONS.UPDATED.equals(action)) {
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
        if (EVENT.REPOSITORY.equals(name)
                || EVENT.PULL_REQUEST.equals(name)) {
            return true;
        }

        return false;
    }

}
