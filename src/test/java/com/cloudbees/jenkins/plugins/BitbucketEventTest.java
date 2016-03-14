package com.cloudbees.jenkins.plugins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
/**
 * Created by Shyri Villar on 11/03/2016.
 */

@RunWith(MockitoJUnitRunner.class)
public class BitbucketEventTest {
    @Mock
    private HttpServletRequest request;

    @Test
    public void testRepositoryEvent() {
        String event = "repo";
        String action = "push";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        assertEquals(event, bitbucketEvent.getName());
        assertEquals(action, bitbucketEvent.getAction());
    }

    @Test
    public void testPullRequestEvent() {
        String event = "pullrequest";
        String action = "approved";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        assertEquals(event, bitbucketEvent.getName());
        assertEquals(action, bitbucketEvent.getAction());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownEvent() {
        String event = "fake";
        String action = "created";

        BitbucketEvent bitbucketEvent = createEvent(event, action);
    }

    @Test
    public void testUnknownAction() {

    }

    private BitbucketEvent createEvent(String event, String action) {
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn(event + ":" + action);

        BitbucketEvent bitbucketEvent = new BitbucketEvent(request.getHeader("x-event-key"));

        return bitbucketEvent;
    }
}
