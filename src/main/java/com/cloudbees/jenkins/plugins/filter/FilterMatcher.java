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

package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerMatcher;
import com.cloudbees.jenkins.plugins.filter.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter matcher
 *
 * @version 2.0
 * @since August 1, 2016
 */
public class FilterMatcher {

    /**
     * Gets the list of {@link BitbucketTriggerFilter} which matches with the event
     *
     * @return the list of {@link BitbucketTriggerFilter} which matches with the event
     */
    public List<BitbucketTriggerFilter> getMatchingFilters(BitbucketEvent bitbucketEvent,
                                                           List<BitbucketTriggerFilter> triggerFilterList) {
        List<BitbucketTriggerFilter> filteredList = null;

        if (triggerFilterList != null) {
            filteredList = new ArrayList<BitbucketTriggerFilter>();
            for (BitbucketTriggerFilter triggerFilter : triggerFilterList) {
                if (matchesEventAndAction(bitbucketEvent, triggerFilter)) {
                    filteredList.add(triggerFilter);
                }
            }
        }

        return filteredList;
    }

    /**
     * Returns {@code true} if the event and action matches
     *
     * @return {@code true} if the event and action matches
     */
    private boolean matchesEventAndAction(BitbucketEvent bitbucketEvent,
                                          BitbucketTriggerFilter triggerFilter) {
        if (BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName()) &&
                triggerFilter instanceof PullRequestTriggerFilter) {
            return new PullRequestTriggerMatcher().matchesAction(bitbucketEvent, triggerFilter);
        } else if (BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName()) &&
                triggerFilter instanceof com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter) {
            return new RepositoryTriggerMatcher().matchesAction(bitbucketEvent, triggerFilter);
        }

        return false;
    }
}
