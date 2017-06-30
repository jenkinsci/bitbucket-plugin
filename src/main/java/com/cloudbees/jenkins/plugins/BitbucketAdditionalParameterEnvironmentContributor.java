package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.cause.pullrequest.PullRequestCause;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BitbucketAdditionalParameterEnvironmentContributor extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(Run run, EnvVars envVars, TaskListener taskListener)
            throws IOException, InterruptedException {

        PullRequestCause cause = (PullRequestCause) run.getCause(PullRequestCause.class);
        if (cause == null) {
            return;
        }

        String sourceBranch = cause.getPullRequestPayLoad().getSourceBranch();
        putEnvVar(envVars, "BITBUCKET_SOURCE_BRANCH", sourceBranch);
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_SOURCE_BRANCH: {0}", sourceBranch);

        String targetBranch = cause.getPullRequestPayLoad().getTargetBranch();
        putEnvVar(envVars, "BITBUCKET_TARGET_BRANCH", targetBranch);
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_TARGET_BRANCH: {0}", targetBranch);
    }

    private static void putEnvVar(EnvVars envs, String name, String value) {
        envs.put(name, getString(value, ""));
    }

    private static String getString(String actual, String d) {
        return actual == null ? d : actual;
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayload.class.getName());
}
