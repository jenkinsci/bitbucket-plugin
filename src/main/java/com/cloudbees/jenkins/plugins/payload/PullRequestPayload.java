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

        String branch = source.getJSONObject("branch").getString("name");
        envVars.put("BITBUCKET_BRANCH", branch);

        String pullRequestUrl = pullRequest.getJSONObject("links").getJSONObject("html").getString("href");
        envVars.put("PULL_REQUEST_LINK", pullRequestUrl);
    }
}
