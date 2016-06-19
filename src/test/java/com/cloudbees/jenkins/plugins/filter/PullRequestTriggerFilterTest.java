package com.cloudbees.jenkins.plugins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestCreatedActionFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import net.sf.json.JSONObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

/**
 * Created by Douglas Miller on 2016/06/13.
 */

@RunWith(MockitoJUnitRunner.class)
public class PullRequestTriggerFilterTest {
    @Mock
    private PullRequestCreatedActionFilter actionFilter;

    @Mock
    private BitbucketPayload bitbucketPayload;

    @Test
    public void testSetDefaultTargetBranch() {
        String targetBranch = "";
        String defaultTargetBranch = "**";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch);

        assertEquals(defaultTargetBranch, triggerFilter.getPullRequestTargetBranch());
    }

    @Test
    public void testSetTargetBranch() {
        String targetBranch = "develop, master";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch);

        assertEquals(targetBranch, triggerFilter.getPullRequestTargetBranch());
    }

    @Test
    public void testShouldScheduleJob() {
        String targetBranch = "develop, master";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "develop");

        assertTrue(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    @Test
    public void testShouldNotScheduleJob() {
        String targetBranch = "master";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "develop");

        assertFalse(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    @Test
    public void testShouldScheduleWildcardJob() {
        String targetBranch = "*/2.0";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "release/2.0");

        assertTrue(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    @Test
    public void testShouldNotScheduleWildcardJob() {
        String targetBranch = "*2.0";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "release/2.0");

        assertFalse(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    @Test
    public void testShouldScheduleDoubleWildcardJob() {
        String targetBranch = "**2.0";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "release/2.0");

        assertTrue(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    @Test
    public void testShouldNotScheduleDoubleWildcardJob() {
        String targetBranch = "**master";

        PullRequestTriggerFilter triggerFilter = createTriggerFilter(targetBranch, "release/2.0");

        assertFalse(triggerFilter.shouldScheduleJob(bitbucketPayload));
    }

    private PullRequestTriggerFilter createTriggerFilter(String pullRequestTargetBranch) {
      return createTriggerFilter(pullRequestTargetBranch, "");
    }

    private PullRequestTriggerFilter createTriggerFilter(String pullRequestTargetBranch, String destinationBranch) {
        when(
          actionFilter.shouldTriggerBuild(
            any(BitbucketPayload.class)
          )
        ).thenReturn(true);

        when(bitbucketPayload.getPayload()).thenReturn(jsonObject(destinationBranch));

        PullRequestTriggerFilter triggerFilter = new PullRequestTriggerFilter(actionFilter, pullRequestTargetBranch);

        return triggerFilter;
    }

    private JSONObject jsonObject(String destinationBranch) {
        JSONObject branch = new JSONObject();
        branch.put("name", destinationBranch);

        JSONObject destination = new JSONObject();
        destination.put("branch", branch);

        JSONObject pullrequest = new JSONObject();
        pullrequest.put("destination", destination);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pullrequest", pullrequest);

        return jsonObject;
    }
}
