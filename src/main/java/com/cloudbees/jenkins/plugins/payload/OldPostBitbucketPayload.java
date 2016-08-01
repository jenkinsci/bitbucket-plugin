package com.cloudbees.jenkins.plugins.payload;

import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * The legacy version of the Bitbucket
 * @since August 1, 2016
 * @version 2.0
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
