package com.cloudbees.jenkins.plugins;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Job;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class BitBucketTriggerRunnableTest {

    @Test
    void triggersWhenNewBranchPushAlsoContainsChanges() {
        BitBucketTriggerRunnable runnable =
                new BitBucketTriggerRunnable("{}", nullJob(), Logger.getAnonymousLogger(), "user", "feature/test", false);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertTrue(runnable.shouldTriggerBuild(true, new PrintStream(output)));
        String log = output.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("Changes found"));
        assertTrue(log.contains("Branch [feature/test] was created"));
    }

    @Test
    void doesNotTriggerForBranchCreationWithoutChangesWhenDisabled() {
        BitBucketTriggerRunnable runnable =
                new BitBucketTriggerRunnable("{}", nullJob(), Logger.getAnonymousLogger(), "user", "feature/test", false);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertFalse(runnable.shouldTriggerBuild(false, new PrintStream(output)));
        String log = output.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("No changes"));
        assertTrue(log.contains("Build on branch created\" is false, not triggering"));
    }

    @Test
    void triggersForBranchCreationWithoutChangesWhenEnabled() {
        BitBucketTriggerRunnable runnable =
                new BitBucketTriggerRunnable("{}", nullJob(), Logger.getAnonymousLogger(), "user", "feature/test", true);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertTrue(runnable.shouldTriggerBuild(false, new PrintStream(output)));
        String log = output.toString(StandardCharsets.UTF_8);
        assertTrue(log.contains("No changes"));
        assertTrue(log.contains("Branch [feature/test] was created"));
    }

    @Test
    void orphanBranchPayloadStillTriggersWhenPollingFindsChangesAndBuildOnCreatedBranchIsFalse() throws IOException {
        assertPayloadTriggersWithChangesWhenBuildOnCreatedBranchIsFalse("bitbucket_branch_created_orphan_payload.json", "empty_branch1");
    }

    @Test
    void branchCreatedWithCommitPayloadStillTriggersWhenPollingFindsChangesAndBuildOnCreatedBranchIsFalse() throws IOException {
        assertPayloadTriggersWithChangesWhenBuildOnCreatedBranchIsFalse("bitbucket_branch_created_with_commit_payload.json", "not_empty1");
    }

    @SuppressWarnings("unchecked")
    private Job<?, ?> nullJob() {
        return (Job<?, ?>) null;
    }

    private void assertPayloadTriggersWithChangesWhenBuildOnCreatedBranchIsFalse(String resourceName, String expectedBranchName)
            throws IOException {
        JSONObject payload = loadPayload(resourceName);
        String branchName = payload.getJSONObject("push")
                .getJSONArray("changes")
                .getJSONObject(0)
                .getJSONObject("new")
                .getString("name");

        BitBucketTriggerRunnable runnable =
                new BitBucketTriggerRunnable(payload.toString(), nullJob(), Logger.getAnonymousLogger(), "user", branchName, false);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertTrue(runnable.shouldTriggerBuild(true, new PrintStream(output)));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Changes found"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Branch [" + expectedBranchName + "] was created"));
    }

    private JSONObject loadPayload(String resourceName) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return JSONObject.fromObject(IOUtils.toString(input, StandardCharsets.UTF_8));
        }
    }
}
