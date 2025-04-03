package com.cloudbees.jenkins.plugins;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitbucketPayloadProcessorTest {

    @Mock private HttpServletRequest request;
    @Mock private BitbucketJobProbe probe;

    private BitbucketPayloadProcessor payloadProcessor;

    @BeforeEach
    void setUp() {
        payloadProcessor = new BitbucketPayloadProcessor(probe);
    }

    @Test
    void testProcessWebhookPayload() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("repo:push");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

        JSONObject payload = new JSONObject()
            .element("actor", new JSONObject()
                .element("nickname", user))
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

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());

        payloadProcessor.processPayload(hgLoad, request);

        verify(probe).triggerMatchingJobs(user, url, "hg", hgLoad.toString());
    }

    @Test
    void processWebhookPayloadBitBucketServer() {
        when(request.getHeader("user-agent")).thenReturn("Apache-HttpClient/4.5.1 (Java/1.8.0_102)");
        when(request.getHeader("x-event-key")).thenReturn("repo:push");

        String user = "test_user";
        String url = "https://bitbucket.org/ce/test_repo";

        JSONObject href = new JSONObject();
        href.element("href", "https://bitbucket.org/projects/CE/repos/test_repo/browse");

        // Set actor and repository so that payload processor will parse as Bitbucket Server Post Webhook payload
        JSONObject payload = new JSONObject()
                .element("actor", new JSONObject()
                        .element("nickname", user))
                .element("repository", new JSONObject()
                        .element("links", new JSONObject()
                                .element("self", new JSONArray()
                                    .element(href)))
                        .element("fullName",  "CE/test_repo"));

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());
    }

    @Test
    void testProcessPostServicePayload() {
        // Ensure header isn't set so that payload processor will parse as old POST service payload
        when(request.getHeader("user-agent")).thenReturn(null);

        JSONObject payload = new JSONObject()
            .element("canon_url", "https://staging.bitbucket.org")
            .element("user", "old_user")
            .element("repository", new JSONObject()
                .element("scm", "git")
                .element("absolute_url", "/old_user/old_repo"));

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs("old_user", "https://staging.bitbucket.org/old_user/old_repo", "git", payload.toString());
    }


    @Test
    void processWebhookPayloadBitBucketSelfHostedPush() throws IOException {
        String user = "user";
        String url = "proj/repository";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("bitbucket_pr_merge_payload.json")) {
        	JSONObject payload = JSONObject.fromObject(IOUtils.toString(input, StandardCharsets.UTF_8));
            payloadProcessor.processPayload(payload, request);

            verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());
        }

    }

    @Test
    void processWebhookPayloadBitBucketSelfHostedPR() throws IOException {
    	String user = "user";
        String url = "proj/repository";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("bitbucket_pr_merge_payload.json")) {
        	JSONObject payload = JSONObject.fromObject(IOUtils.toString(input, StandardCharsets.UTF_8));
            payloadProcessor.processPayload(payload, request);

            verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());
        }
    }


    @Test
    void testProcessWebhookPayload_inCaseOwnerUsernameFieldIsReplacedByNickName() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("repo:push");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

        JSONObject payload = new JSONObject()
                .element("actor", new JSONObject()
                        .element("nickname", user))
                .element("repository", new JSONObject()
                        .element("links", new JSONObject()
                                .element("html", new JSONObject()
                                        .element("href", url))));

        JSONObject hgLoad = new JSONObject()
                .element("scm", "hg")
                .element("owner", new JSONObject()
                        .element("nickname", user))
                .element("links", new JSONObject()
                        .element("html", new JSONObject()
                                .element("href", url)));

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());

        payloadProcessor.processPayload(hgLoad, request);

        verify(probe).triggerMatchingJobs(user, url, "hg", hgLoad.toString());
    }
}
