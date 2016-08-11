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

import com.cloudbees.jenkins.plugins.processor.BitbucketPayloadProcessor;
import com.cloudbees.jenkins.plugins.processor.BitbucketPayloadProcessorFactory;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * BitbucketHookReceiver which processes HTTP POST packages to $JENKINS_URL/bitbucket-hook
 * @version 1.0
 */
@Extension
public class BitbucketHookReceiver implements UnprotectedRootAction {
    private BitbucketPayloadProcessorFactory payloadProcessorFactory = new BitbucketPayloadProcessorFactory();

    public static final String BITBUCKET_HOOK_URL = "bitbucket-hook";

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return BITBUCKET_HOOK_URL;
    }

    /**
     * Bitbucket send <a href="https://confluence.atlassian.com/display/BITBUCKET/Write+brokers+(hooks)+for+Bitbucket">payload</a>
     * as form-urlencoded <pre>payload=JSON</pre>
     * @throws IOException
     */
    public void doIndex(StaplerRequest req) throws IOException {
        String body = IOUtils.toString(req.getInputStream());
        if (!body.isEmpty() && req.getRequestURI().contains("/" + BITBUCKET_HOOK_URL + "/")) {
            String contentType = req.getContentType();
            if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
                body = URLDecoder.decode(body);
            }
            if (body.startsWith("payload=")) body = body.substring(8);

            LOGGER.log(Level.FINE, "Received commit hook notification : {0}", body);
            JSONObject payload = JSONObject.fromObject(body);

            if ("Bitbucket-Webhooks/2.0".equals(req.getHeader("user-agent"))) {
                BitbucketEvent bitbucketEvent = new BitbucketEvent(req.getHeader("x-event-key"));
                BitbucketPayloadProcessor bitbucketPayloadProcessor = payloadProcessorFactory.create(bitbucketEvent);
                bitbucketPayloadProcessor.processPayload(payload);
            } else {
                LOGGER.log(Level.INFO, "Processing old POST service payload");
                BitbucketPayloadProcessor bitbucketPayloadProcessor =
                        payloadProcessorFactory.createOldProcessor(new BitbucketEvent("repo:push"));
                bitbucketPayloadProcessor.processPayload(payload);
            }
        } else {
            LOGGER.log(Level.WARNING, "The Jenkins job cannot be triggered. You might no have configured correctly the WebHook on BitBucket with the last slash `http://<JENKINS-URL>/bitbucket-hook/`");
        }

    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketHookReceiver.class.getName());
}
