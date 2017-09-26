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

package com.cloudbees.jenkins.plugins.extensions.dsl;

import com.cloudbees.jenkins.plugins.BitBucketTrigger;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestApprovedActionFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestCreatedActionFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestUpdatedActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryPushActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter;
import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code BitbucketHookJobDslExtension} to generate the triggers with job-dsl plugin
 * @since August 1, 2016
 * @version 2.0
 */
@Extension(optional = true)
public class BitbucketHookJobDslExtension extends ContextExtensionPoint {

    @Deprecated
    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketPush() {
        return bitbucketRepositoryPushAction();
    }

    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketRepositoryPushAction() {
        List<BitbucketTriggerFilter> triggers;
        RepositoryPushActionFilter repositoryPushActionFilter = new RepositoryPushActionFilter();
        RepositoryTriggerFilter repositoryTriggerFilter = new RepositoryTriggerFilter(repositoryPushActionFilter);
        triggers = new ArrayList<BitbucketTriggerFilter>();
        triggers.add(repositoryTriggerFilter);
        return new BitBucketTrigger(triggers);
    }

    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketPullRequestApprovedAction(boolean onlyIfReviewersApproved) {
        List<BitbucketTriggerFilter> triggers;
        PullRequestApprovedActionFilter pullRequestApprovedActionFilter = new PullRequestApprovedActionFilter(onlyIfReviewersApproved);
        PullRequestTriggerFilter pullRequestTriggerFilter = new PullRequestTriggerFilter(pullRequestApprovedActionFilter);
        triggers = new ArrayList<BitbucketTriggerFilter>();
        triggers.add(pullRequestTriggerFilter);
        return new BitBucketTrigger(triggers);
    }

    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketPullRequestCreatedAction() {
        List<BitbucketTriggerFilter> triggers;
        PullRequestCreatedActionFilter pullRequestCreatedActionFilter = new PullRequestCreatedActionFilter();
        PullRequestTriggerFilter pullRequestTriggerFilter = new PullRequestTriggerFilter(pullRequestCreatedActionFilter);
        triggers = new ArrayList<BitbucketTriggerFilter>();
        triggers.add(pullRequestTriggerFilter);
        return new BitBucketTrigger(triggers);
    }

    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketPullRequestUpdatedAction() {
        List<BitbucketTriggerFilter> triggers;
        PullRequestUpdatedActionFilter pullRequestUpdatedActionFilter = new PullRequestUpdatedActionFilter();
        PullRequestTriggerFilter pullRequestTriggerFilter = new PullRequestTriggerFilter(pullRequestUpdatedActionFilter);
        triggers = new ArrayList<BitbucketTriggerFilter>();
        triggers.add(pullRequestTriggerFilter);
        return new BitBucketTrigger(triggers);
    }

}


