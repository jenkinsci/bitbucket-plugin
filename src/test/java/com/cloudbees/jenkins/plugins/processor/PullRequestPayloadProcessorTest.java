package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.*;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import net.sf.json.JSONObject;
import org.junit.Before;
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

/**
 * Created by isvillar on 11/03/2016.
 */
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

        JSONObject hgLoad = new JSONObject()
                .element("scm", "hg")
                .element("owner", new JSONObject()
                        .element("username", user))
                .element("links", new JSONObject()
                        .element("html", new JSONObject()
                                .element("href", url)));

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
