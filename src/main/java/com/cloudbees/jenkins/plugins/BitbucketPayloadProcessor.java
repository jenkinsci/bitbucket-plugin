package com.cloudbees.jenkins.plugins;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;

public class BitbucketPayloadProcessor {

    private final BitbucketJobProbe probe;

    public BitbucketPayloadProcessor(BitbucketJobProbe probe) {
        this.probe = probe;
    }

    public BitbucketPayloadProcessor() {
        this(new BitbucketJobProbe());
    }

    public void processPayload(JSONObject payload, HttpServletRequest request) {
        if ("Bitbucket-Webhooks/2.0".equals(request.getHeader("user-agent"))) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                LOGGER.info("Processing new Webhooks payload");
                processWebhookPayload(payload);
            }
        } else {
            LOGGER.info("Processing old POST service payload");
            processPostServicePayload(payload);
        }
    }

    private void processWebhookPayload(JSONObject payload) {
        if (payload.has("repository")) {
            JSONObject repo = payload.getJSONObject("repository");
            LOGGER.info("Received commit hook notification for "+repo);

            String user = payload.getJSONObject("actor").getString("username");
            String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = repo.has("scm") ? repo.getString("scm") : "git";

            probe.triggerMatchingJobs(user, url, scm);
        } else if (payload.has("scm")) {
            LOGGER.info("Received commit hook notification for hg " + payload);
            String user = payload.getJSONObject("owner").getString("username");
            String url = payload.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = payload.has("scm") ? payload.getString("scm") : "hg";

            probe.triggerMatchingJobs(user, url, scm);
        }

    }

    private void processPostServicePayload(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        LOGGER.info("Received commit hook notification for "+repo);

        String user = payload.getString("user");
        String url = payload.getString("canon_url") + repo.getString("absolute_url");
        String scm = repo.getString("scm");

        probe.triggerMatchingJobs(user, url, scm);
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayloadProcessor.class.getName());

}
