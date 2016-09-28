/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
