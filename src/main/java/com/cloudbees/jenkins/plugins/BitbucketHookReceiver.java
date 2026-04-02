package com.cloudbees.jenkins.plugins;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import net.sf.json.JSONObject;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BitbucketHookReceiver extends BitbucketCrumbExclusion implements UnprotectedRootAction {

    private final BitbucketPayloadProcessor payloadProcessor = new BitbucketPayloadProcessor();
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
    public void doIndex(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException {
        byte[] bodyBytes = IOUtils.toByteArray(req.getInputStream());
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        if (!body.isEmpty() && req.getRequestURI().contains("/" + BITBUCKET_HOOK_URL + "/")) {
            String contentType = req.getContentType();
            if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
                body = URLDecoder.decode(body, StandardCharsets.UTF_8);
            }
            if (body.startsWith("payload=")) body = body.substring(8);

            LOGGER.log(Level.FINE, "Received commit hook notification : {0}", body);
            JSONObject payload = JSONObject.fromObject(body);

            BitbucketWebhookResult result = payloadProcessor.processPayload(payload, req, bodyBytes);
            if (result == BitbucketWebhookResult.INVALID_SIGNATURE) {
                LOGGER.log(Level.WARNING, "Rejected Bitbucket webhook with invalid or missing X-Hub-Signature");
                rsp.sendError(403, "Invalid Bitbucket webhook signature");
            } else if (result == BitbucketWebhookResult.NO_MATCH) {
                LOGGER.log(Level.WARNING, "No matching Jenkins job or multibranch project was found for the Bitbucket webhook");
            }
        } else {
            LOGGER.log(Level.WARNING, "The Jenkins job cannot be triggered. You might not have configured correctly the WebHook on BitBucket with the last slash `http://<JENKINS-URL>/bitbucket-hook/` or a 'Test connection' invocation of the hook was triggered.");
        }

    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketHookReceiver.class.getName());
}
