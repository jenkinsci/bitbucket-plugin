package com.cloudbees.jenkins.plugins;

import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;

/**
 * Created by isvillar on 11/03/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class PullRequestPayloadProcessorTest {
    @Mock private HttpServletRequest request;
    @Mock private BitbucketJobProbe probe;

    private BitbucketPayloadProcessor payloadProcessor;

    @Before
    public void setUp() {
        payloadProcessor = new BitbucketPayloadProcessor(probe);
    }

    @Test
    public void testProcessPullRequestApprovalWebhook() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("pullrequest:approval");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

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
    }
}
