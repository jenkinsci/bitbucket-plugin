package com.cloudbees.jenkins.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
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
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayload(payload);
            }
        } else if (payload.has("actor") && payload.has("repository") && payload.getJSONObject("repository").has("links")) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                // found web hook according to https://support.atlassian.com/bitbucket-cloud/docs/event-payloads/
                LOGGER.log(Level.INFO, "Processing new Cloud Webhooks payload");
                processWebhookPayloadBitBucketServer(payload);
            } else if ("repo:refs_changed".equals(request.getHeader("x-event-key"))) {
                // found web hook according to https://confluence.atlassian.com/bitbucketserver/event-payload-938025882.html
                LOGGER.log(Level.INFO, "Processing new Self Hosted Server Webhooks payload");
                processWebhookPayloadBitBucketSelfHosted(payload);
            } else {
                LOGGER.log(Level.INFO, "Unsupported [x-event-key] value, [x-event-key] is [" + request.getHeader("x-event-key") + "]");
            }
        } else if (payload.has("actor")) {
        	// we assume that the passed hook was from bitbucket server https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html
        	LOGGER.log(Level.INFO, "Processing webhook for self-hosted bitbucket instance");
        	processWebhookPayloadBitBucketSelfHosted(payload);
        } else {
            // https://github.com/jenkinsci/bitbucket-plugin/pull/65
            if ("diagnostics:ping".equals(request.getHeader("x-event-key"))) {
                if (payload.has("test") && payload.getBoolean("test")) {
                    LOGGER.log(Level.INFO, "Bitbucket test connection payload");
                    return;
                }
            }
            LOGGER.log(Level.INFO, "Processing old POST service payload");
            processPostServicePayload(payload);
        }
    }

    /**
     * parses the payload from self-hosted bitbucket instance which uses the default webhooks implementation.
     * 
     * https://confluence.atlassian.com/bitbucketserver0510/managing-webhooks-in-bitbucket-server-951390737.html
     * https://confluence.atlassian.com/bitbucketserver0510/event-payload-951390742.html
     * 
     * @param payload The payload matching the definition in https://confluence.atlassian.com/bitbucketserver0510/event-payload-951390742.html
     */
    private void processWebhookPayloadBitBucketSelfHosted(JSONObject payload) {
    	JSONObject repo;
    	
    	// find the repository hidden in different objects
    	if (payload.has("repository")) { // for push to repository
    		repo = payload.getJSONObject("repository");
    	} else if (payload.has("pullRequest")) { // for all PR events
    		repo = payload.getJSONObject("pullRequest").getJSONObject("toRef").getJSONObject("repository");
    	} else {
    		LOGGER.log(Level.WARNING, "Not possible to trigger job for event '{0}'. Only PR events and pushes are supported for now.", payload.get("eventKey"));
    		LOGGER.log(Level.FINE, payload.toString());
    		return;
    	}
    	
        String user = payload.getJSONObject("actor").getString("name");
        String url = repo.getJSONObject("project").getString("key").toLowerCase() + "/" + repo.getString("slug");

        // always use git no other repo type supported on self-hosted solution
        String scm = "git";
        probe.triggerMatchingJobs(user, url, scm, payload.toString());
		
	}

	private void processWebhookPayload(JSONObject payload) {
        if (isPayloadOldMemberNull(payload)){
            String branchName = getBranchName(payload);
            JSONObject repo = payload.getJSONObject("repository");
            LOGGER.log(Level.INFO, "Branch [" +branchName + "] was created");
            String user = getUser(payload, "actor");
            String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = repo.has("scm") ? repo.getString("scm") : "git";

            probe.triggerMatchingJobs(user, url, scm, payload.toString(), branchName);
        } else {
            if (payload.has("repository")) {
                JSONObject repo = payload.getJSONObject("repository");
                LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

                String user = getUser(payload, "actor");
                String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
                String scm = repo.has("scm") ? repo.getString("scm") : "git";

                probe.triggerMatchingJobs(user, url, scm, payload.toString());
            } else if (payload.has("scm")) {
                LOGGER.log(Level.INFO, "Received commit hook notification for hg: {0}", payload);
                String user = getUser(payload, "owner");
                String url = payload.getJSONObject("links").getJSONObject("html").getString("href");
                String scm = payload.has("scm") ? payload.getString("scm") : "hg";

                probe.triggerMatchingJobs(user, url, scm, payload.toString());
            }
       }

    }

    private String getBranchName(JSONObject payload) {
        if (payload.has("push")) {
            LOGGER.log(Level.INFO, "found [push] in payload");
            JSONObject jsonObjectPush = payload.getJSONObject("push");
            if (jsonObjectPush.has("changes")) {
                LOGGER.log(Level.INFO, "found [push/changes] in payload");
                JSONArray jsonArrayChanges = jsonObjectPush.getJSONArray("changes");

                for (Object jsonArrayChange : jsonArrayChanges) {
                    JSONObject jsonObject = (JSONObject) jsonArrayChange;
                    if (jsonObject.has("new")) {
                        JSONObject jsonObjectNew = jsonObject.getJSONObject("new");
                        if (jsonObjectNew.has("name")) {
                            return jsonObjectNew.getString("name");
                        }

                    }
                }
            }
        }
        return "";
    }

    private boolean isPayloadOldMemberNull(JSONObject payload) {
        if ( payload.has("push")){
            LOGGER.log(Level.INFO, "found [push] in payload");
            JSONObject jsonObjectPush = payload.getJSONObject("push");
            if (jsonObjectPush.has("changes")){
                LOGGER.log(Level.INFO, "found [push/changes] in payload");
                JSONArray jsonArrayChanges = jsonObjectPush.getJSONArray("changes");

                for (Object jsonArrayChange : jsonArrayChanges) {
                    JSONObject jsonObject = (JSONObject) jsonArrayChange;
                    return jsonObject.get("old") instanceof JSONNull;
                }
            }
        }
        return false;
    }

    private String getUser(JSONObject payload, String jsonObject) {
        String user;
        try {
            user = payload.getJSONObject(jsonObject).getString("username");
        } catch (JSONException e1) {
            try {
                user = payload.getJSONObject(jsonObject).getString("nickname");
            } catch (JSONException e2) {
                user = payload.getJSONObject(jsonObject).getString("display_name");
            }
        }
        return user;
    }

    /**
     * Payload processor for BitBucket server. The plugin Post Webhooks for Bitbucket
     * https://marketplace.atlassian.com/plugins/nl.topicus.bitbucket.bitbucket-webhooks/server/overview
     * should be installed and configured
     *
     * @param payload JSON object
     */
    private void processWebhookPayloadBitBucketServer(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        String user = getUser(payload, "actor");
        String url = "";
        if (repo.getJSONObject("links").getJSONArray("self").size() != 0) {
            try {
                URL pushHref = new URL(repo.getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href"));
                url = pushHref.toString().replaceFirst("projects.*", repo.getString("fullName").toLowerCase());
                String scm = repo.has("scmId") ? repo.getString("scmId") : "git";
                probe.triggerMatchingJobs(user, url, scm, payload.toString());
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, String.format("URL %s is malformed", url), e);
            }
        }
    }

    private void processPostServicePayload(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

        String user = payload.getString("user");
        String url = payload.getString("canon_url") + repo.getString("absolute_url");
        String scm = repo.getString("scm");

        probe.triggerMatchingJobs(user, url, scm, payload.toString());
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayloadProcessor.class.getName());

}
