package com.cloudbees.jenkins.plugins.payload;

import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Created by Shyri Villar on 15/03/2016.
 */
public class RepositoryPayload extends BitbucketPayload {
    public RepositoryPayload(@Nonnull JSONObject payload) {
        super(payload);
    }
}
