package com.cloudbees.jenkins.plugins.payload;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Created by Shryi Villar on 11/03/2016.
 */
public class PullRequestPayload extends BitBucketPayload {
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
    }
}
