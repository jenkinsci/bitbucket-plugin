package com.cloudbees.jenkins.plugins.payload;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Bitbucket payload
 * @since August 1, 2016
 * @version 2.0
 */
public class BitbucketPayload extends InvisibleAction implements EnvironmentContributingAction {
    protected final @Nonnull JSONObject payload;
    protected String scm;
    protected String user;
    protected String scmUrl;

    public BitbucketPayload(@Nonnull JSONObject payload) {
        this.payload = payload;

        if (payload.has("repository")) {
            JSONObject repository = payload.getJSONObject("repository");
            JSONObject actor = payload.getJSONObject("actor");

            this.user = actor.getString("username");
            this.scm = repository.has("scm") ? repository.getString("scm") : "git";
            this.scmUrl = repository.getJSONObject("links").getJSONObject("html").getString("href");
        } else if (payload.has("scm")) {
            LOGGER.log(Level.INFO, "Received commit hook notification for hg: {0}", payload);
            this.user = payload.getJSONObject("owner").getString("username");
            this.scmUrl = payload.getJSONObject("links").getJSONObject("html").getString("href");
            this.scm = payload.has("scm") ? payload.getString("scm") : "hg";
        }
    }

    @Nonnull
    public JSONObject getPayload() {
        return payload;
    }

    public String getScm() {
        return scm;
    }

    public String getUser() {
        return user;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        envVars.put("BITBUCKET_PAYLOAD", payload.toString());
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payload);
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayload.class.getName());
}
