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

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
    public void testRepositoryEventPushAction() {
        String event = "repo";
        String action = "push";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        assertEquals(event, bitbucketEvent.getName());
        assertEquals(action, bitbucketEvent.getAction());
    }

    @Test
    public void testPullRequestEventCreated() {
        String event = "pullrequest";
        String action = "created";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        assertEquals(event, bitbucketEvent.getName());
        assertEquals(action, bitbucketEvent.getAction());
    }

    @Test
    public void testPullRequestEventUpdated() {
        String event = "pullrequest";
        String action = "updated";

        BitbucketEvent bitbucketEvent = createEvent(event, action);

        assertEquals(event, bitbucketEvent.getName());
        assertEquals(action, bitbucketEvent.getAction());
    }

    @Test
    public void testPullRequestEventApprovedAction() {
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

    @Test(expected = UnsupportedOperationException.class)
    public void testUnknownAction() {
        String event = "repo";
        String action = "fake";

        BitbucketEvent bitbucketEvent = createEvent(event, action);
    }

    private BitbucketEvent createEvent(String event, String action) {
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn(event + ":" + action);

        BitbucketEvent bitbucketEvent = new BitbucketEvent(request.getHeader("x-event-key"));

        return bitbucketEvent;
    }

}
