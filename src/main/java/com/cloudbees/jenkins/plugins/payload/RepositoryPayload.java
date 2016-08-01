package com.cloudbees.jenkins.plugins.payload;

import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Represents the payload of the reposirory
 * @since August 1, 2016
 * @version 2.0
 */
public class RepositoryPayload extends BitbucketPayload {
    public RepositoryPayload(@Nonnull JSONObject payload) {
        super(payload);
    }
}
