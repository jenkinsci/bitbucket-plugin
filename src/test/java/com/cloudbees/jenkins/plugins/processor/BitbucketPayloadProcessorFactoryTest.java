package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by isvillar on 14/03/2016.
 */
public class BitbucketPayloadProcessorFactoryTest {
    @Mock
    private HttpServletRequest request;
    @Mock private BitbucketJobProbe probe;

    private BitbucketPayloadProcessorFactory payloadProcessorFactory;

    @Before
    public void setUp() {
        payloadProcessorFactory = new BitbucketPayloadProcessorFactory();
    }

    @Test
    public void testCreateRepositoryPayloadProcessor() {
        String event = "repo";
        String action = "push";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        BitbucketPayloadProcessor payloadProcessor = payloadProcessorFactory.create(bitbucketEvent);

        assertThat(payloadProcessor, is(notNullValue()));
        assertThat(payloadProcessor, is(instanceOf(RepositoryPayloadProcessor.class)));
    }

    @Test
    public void testCreatePullRequestPayloadProcessor() {
        String event = "pullrequest";
        String action = "created";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        BitbucketPayloadProcessor payloadProcessor = payloadProcessorFactory.create(bitbucketEvent);

        assertThat(payloadProcessor, is(notNullValue()));
        assertThat(payloadProcessor, is(instanceOf(PullRequestPayloadProcessor.class)));
    }

    private BitbucketEvent createEvent(String event, String action) {
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn(event + ":" + action);

        BitbucketEvent bitbucketEvent = new BitbucketEvent(request.getHeader("x-event-key"));

        return bitbucketEvent;
    }
}
