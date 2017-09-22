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

package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.*;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestPayloadProcessorTest {
    @Mock private HttpServletRequest request;
    @Mock private BitbucketJobProbe probe;

    @Captor private ArgumentCaptor<BitbucketPayload> payloadCaptor;
    @Captor private ArgumentCaptor<BitbucketEvent> eventCaptor;

    private PullRequestPayloadProcessor pullRequestPayloadProcessor;

    @Test
    public void testProcessPullRequestApprovalWebhookGit() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("pullrequest:approved");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

        BitbucketEvent bitbucketEvent = new BitbucketEvent(request.getHeader("x-event-key"));

        pullRequestPayloadProcessor = new PullRequestPayloadProcessor(probe, bitbucketEvent);

        JSONObject payload = new JSONObject()
                .element("actor", new JSONObject()
                        .element("username", user))
                .element("repository", new JSONObject()
                        .element("links", new JSONObject()
                                .element("html", new JSONObject()
                                        .element("href", url))));

        pullRequestPayloadProcessor.processPayload(payload);

        verify(probe).triggetMatchingJobs(eventCaptor.capture(), payloadCaptor.capture());

        assertEquals(bitbucketEvent, eventCaptor.getValue());
        assertEquals(payload, payloadCaptor.getValue().getPayload());
    }

    @Test
    public void testProcessPullRequestApprovalWebhookHg() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("pullrequest:approved");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

        BitbucketEvent bitbucketEvent = new BitbucketEvent(request.getHeader("x-event-key"));

        pullRequestPayloadProcessor = new PullRequestPayloadProcessor(probe, bitbucketEvent);

        JSONObject hgLoad = new JSONObject()
                .element("scm", "hg")
                .element("owner", new JSONObject()
                        .element("username", user))
                .element("links", new JSONObject()
                        .element("html", new JSONObject()
                                .element("href", url)));

        pullRequestPayloadProcessor.processPayload(hgLoad);

        verify(probe).triggetMatchingJobs(eventCaptor.capture(), payloadCaptor.capture());

        assertEquals(bitbucketEvent, eventCaptor.getValue());
        assertEquals(hgLoad, payloadCaptor.getValue().getPayload());
    }
}
