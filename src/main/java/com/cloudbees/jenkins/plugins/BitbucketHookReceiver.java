package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BitbucketHookReceiver implements UnprotectedRootAction {

    private final BitbucketPayloadProcessor payloadProcessor = new BitbucketPayloadProcessor();

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "bitbucket-hook";
    }

    /**
     * Bitbucket send <a href="https://confluence.atlassian.com/display/BITBUCKET/Write+brokers+(hooks)+for+Bitbucket">payload</a>
     * as form-urlencoded <pre>payload=JSON</pre>
     * @throws IOException
     */
    public void doIndex(StaplerRequest req) throws IOException {
        String body = IOUtils.toString(req.getInputStream());
        String contentType = req.getContentType();
        if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            body = URLDecoder.decode(body);
        }
        if (body.startsWith("payload=")) body = body.substring(8);

        LOGGER.fine("Received commit hook notification : " + body);
        JSONObject payload = JSONObject.fromObject(body);

        payloadProcessor.processPayload(payload, req);
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketHookReceiver.class.getName());

}
