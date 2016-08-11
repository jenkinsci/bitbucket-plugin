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

package com.cloudbees.jenkins.plugins.filter.pullrequest;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilterDescriptor;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The base {link BitbucketTriggerFilter} for pull requests
 * @since August 1, 2016
 * @version 2.0
 */
public class PullRequestTriggerFilter extends BitbucketTriggerFilter {
    public PullRequestActionFilter actionFilter;

    @DataBoundConstructor
    public PullRequestTriggerFilter(PullRequestActionFilter actionFilter) {
        this.actionFilter = actionFilter;
    }


    @Override
    public boolean shouldScheduleJob(BitbucketPayload bitbucketPayload) {
        return actionFilter.shouldTriggerBuild(bitbucketPayload);
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitbucketPayload pullRequestPayload) throws IOException {
        return actionFilter.getCause(pollingLog, pullRequestPayload);
    }

    @Extension
    public static class FilterDescriptorImpl extends BitbucketTriggerFilterDescriptor {
        public String getDisplayName() { return "Pull Request"; }

        public List<PullRequestActionDescriptor> getActionDescriptors() {
            // you may want to filter this list of descriptors here, if you are being very fancy
            return Jenkins.getInstance().getDescriptorList(PullRequestActionFilter.class);
        }
    }

    public PullRequestActionFilter getActionFilter() {
        return actionFilter;
    }

}
