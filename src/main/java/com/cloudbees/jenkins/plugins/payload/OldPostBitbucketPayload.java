package com.cloudbees.jenkins.plugins.payload;

import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Created by isvillar on 15/03/2016.
 */
public class OldPostBitbucketPayload extends BitbucketPayload {

    public OldPostBitbucketPayload(@Nonnull JSONObject payload) {
        super(payload);

        JSONObject repo = payload.getJSONObject("repository");

        this.user = payload.getString("user");
        this.scmUrl = payload.getString("canon_url") + repo.getString("absolute_url");
        this.scm = repo.getString("scm");
    }
}
