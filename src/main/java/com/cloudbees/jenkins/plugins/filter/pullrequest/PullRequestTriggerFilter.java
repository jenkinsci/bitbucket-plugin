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

import net.sf.json.JSONObject;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

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
    public String pullRequestTargetBranch;

    @DataBoundConstructor
    public PullRequestTriggerFilter(PullRequestActionFilter actionFilter, String pullRequestTargetBranch) {
        this.actionFilter = actionFilter;
        setPullRequestTargetBranch(pullRequestTargetBranch);
    }


    @Override
    public boolean shouldScheduleJob(BitbucketPayload bitbucketPayload) {
        if (destinationMatchesTarget(bitbucketPayload)) {
          return actionFilter.shouldTriggerBuild(bitbucketPayload);
        }
        return false;
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

    public void setPullRequestTargetBranch(String pullRequestTargetBranch) {
      if(pullRequestTargetBranch == null)
            throw new IllegalArgumentException();
        else if(pullRequestTargetBranch.length() == 0)
            this.pullRequestTargetBranch = "**";
        else
            this.pullRequestTargetBranch = pullRequestTargetBranch.trim();
    }

    public String getPullRequestTargetBranch() {
        return pullRequestTargetBranch;
    }

    protected boolean destinationMatchesTarget(BitbucketPayload pullRequestPayload) {
        JSONObject pullRequest = pullRequestPayload.getPayload().getJSONObject("pullrequest");
        JSONObject destination = pullRequest.getJSONObject("destination");

        for (String target: pullRequestTargetBranch.split(",")) {
          target = target.trim();
          Pattern pattern = getPattern(target);
          String name = destination.getJSONObject("branch").getString("name");
          if (pattern.matcher(name).matches()) {
            return true;
          }
        }

        return false;
    }

    protected Pattern getPattern(String input) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(input, "*", true);
        boolean previousWildcard = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("*")) {
                if (previousWildcard) {
                    builder.append(".*");
                    previousWildcard = false;
                }
                else {
                    previousWildcard = true;
                }
            }
            else {
                builder.append(Pattern.quote(token));
            }
        }
        return Pattern.compile(builder.toString());
    }

}
