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

package com.cloudbees.jenkins.plugins.payload;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Represents the payload of the pull request
 * @since August 1, 2016
 * @version 2.0
 */
public class PullRequestPayload extends BitbucketPayload {
    public PullRequestPayload(@Nonnull JSONObject payload) {
        super(payload);
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        super.buildEnvVars(abstractBuild, envVars);

        JSONObject pullRequest = payload.getJSONObject("pullrequest");
        JSONObject source = pullRequest.getJSONObject("source");
        JSONObject destination = pullRequest.getJSONObject("destination");

        String branch = source.getJSONObject("branch").getString("name");
        envVars.put("BITBUCKET_BRANCH", branch);

        String targetBranch = destination.getJSONObject("branch").getString("name");
        envVars.put("BITBUCKET_TARGET_BRANCH", targetBranch);

        String pullRequestUrl = pullRequest.getJSONObject("links").getJSONObject("html").getString("href");
        envVars.put("PULL_REQUEST_LINK", pullRequestUrl);
    }

}
