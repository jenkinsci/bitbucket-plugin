package com.cloudbees.jenkins.plugins.payload;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Created by Shyri Villar on 15/03/2016.
 */
public class RepositoryPayload extends BitBucketPayload {
    public RepositoryPayload(@Nonnull JSONObject payload) {
        super(payload);
    }
}
