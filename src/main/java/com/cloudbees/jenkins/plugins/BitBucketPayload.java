package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import hudson.model.Run;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inject the payload received by BitBucket into the build through $BITBUCKET_PAYLOAD so it can be processed
 * @since January 9, 2016
 * @version 1.1.5
 */
public class BitBucketPayload extends InvisibleAction implements EnvironmentContributingAction {
    private final @Nonnull String payload;

    public BitBucketPayload(@Nonnull String payload) {
        this.payload = payload;
    }

    @Nonnull
    public String getPayload() {
        return payload;
    }

    @Override
    public void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars env) {
        EnvironmentContributingAction.super.buildEnvironment(run, env);
        final String payload = getPayload();
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payload);
        env.put("BITBUCKET_PAYLOAD", payload);
    }

    private static final Logger LOGGER = Logger.getLogger(BitBucketPayload.class.getName());
}
