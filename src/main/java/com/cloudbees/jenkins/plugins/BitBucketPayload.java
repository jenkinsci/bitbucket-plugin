package com.cloudbees.jenkins.plugins;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import net.sf.json.JSONObject;

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
	public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
		final String payload = getPayload();
		LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payload);
		envVars.put("BITBUCKET_PAYLOAD", payload);
		envVars.put("BRANCH", getBranchName(payload));
	}

	private String getBranchName(String body) {
		String result = "";
		try {
			JSONObject payload = JSONObject.fromObject(body);
			result = payload.getJSONObject("push").getJSONArray("changes").getJSONObject(0).getJSONObject("old").getString("name");
		} catch (Exception e) {
			LOGGER.log(Level.FINEST, "No branch name found");
		}
		return result;
	}

	private static final Logger LOGGER = Logger.getLogger(BitBucketPayload.class.getName());
}
