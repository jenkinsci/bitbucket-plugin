package com.cloudbees.jenkins.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;

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

    public BitbucketWebhookResult processPayload(JSONObject payload, HttpServletRequest request, byte[] bodyBytes) {
        if ("Bitbucket-Webhooks/2.0".equals(request.getHeader("user-agent"))) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                LOGGER.log(Level.FINER, "Processing new Webhooks payload");
                return processWebhookPayload(payload, request, bodyBytes);
            }
        } else if (payload.has("actor") && payload.has("repository") && payload.getJSONObject("repository").has("links")) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                // found web hook according to https://support.atlassian.com/bitbucket-cloud/docs/event-payloads/
                LOGGER.log(Level.FINER, "Processing new Cloud Webhooks payload");
                return processWebhookPayloadBitBucketServer(payload, request, bodyBytes);
            } else if ("repo:refs_changed".equals(request.getHeader("x-event-key"))) {
                // found web hook according to https://confluence.atlassian.com/bitbucketserver/event-payload-938025882.html
                LOGGER.log(Level.FINER, "Processing new Self Hosted Server Webhooks payload");
                return processWebhookPayloadBitBucketSelfHosted(payload, request, bodyBytes);
            } else {
                LOGGER.log(Level.FINER, "Unsupported [x-event-key] value, [x-event-key] is [" + request.getHeader("x-event-key") + "]");
            }
        } else if (payload.has("actor")) {
        	// we assume that the passed hook was from bitbucket server https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html
        	LOGGER.log(Level.FINER, "Processing webhook for self-hosted bitbucket instance");
        	return processWebhookPayloadBitBucketSelfHosted(payload, request, bodyBytes);
        } else {
            // https://github.com/jenkinsci/bitbucket-plugin/pull/65
            if ("diagnostics:ping".equals(request.getHeader("x-event-key"))) {
                if (payload.has("test") && payload.getBoolean("test")) {
                    LOGGER.log(Level.FINER, "Bitbucket test connection payload");
                    return BitbucketWebhookResult.IGNORED;
                }
            }
            LOGGER.log(Level.FINER, "Processing old POST service payload");
            return processPostServicePayload(payload, request, bodyBytes);
        }
        return BitbucketWebhookResult.IGNORED;
    }

    /**
     * parses the payload from self-hosted bitbucket instance which uses the default webhooks implementation.
     * 
     * https://confluence.atlassian.com/bitbucketserver0510/managing-webhooks-in-bitbucket-server-951390737.html
     * https://confluence.atlassian.com/bitbucketserver0510/event-payload-951390742.html
     * 
     * @param payload The payload matching the definition in https://confluence.atlassian.com/bitbucketserver0510/event-payload-951390742.html
     */
    private BitbucketWebhookResult processWebhookPayloadBitBucketSelfHosted(JSONObject payload, HttpServletRequest request, byte[] bodyBytes) {
    	JSONObject repo;
    	
    	// find the repository hidden in different objects
    	if (payload.has("repository")) { // for push to repository
    		repo = payload.getJSONObject("repository");
    	} else if (payload.has("pullRequest")) { // for all PR events
    		repo = payload.getJSONObject("pullRequest").getJSONObject("toRef").getJSONObject("repository");
    	} else {
    		LOGGER.log(Level.WARNING, "Not possible to trigger job for event '{0}'. Only PR events and pushes are supported for now.", payload.get("eventKey"));
    		LOGGER.log(Level.FINE, payload.toString());
    		return BitbucketWebhookResult.IGNORED;
    	}
    	
        String user = payload.getJSONObject("actor").getString("name");
        String url = repo.getJSONObject("project").getString("key").toLowerCase() + "/" + repo.getString("slug");

        // always use git no other repo type supported on self-hosted solution
        String scm = "git";
        return probe.triggerMatchingJobs(user, url, scm, payload.toString(), null, request.getHeader("X-Hub-Signature"), bodyBytes);
		
	}

	private BitbucketWebhookResult processWebhookPayload(JSONObject payload, HttpServletRequest request, byte[] bodyBytes) {
        if (isPayloadOldMemberNull(payload)){
            String branchName = getBranchName(payload);
            JSONObject repo = payload.getJSONObject("repository");
            LOGGER.log(Level.FINER, "Branch [" +branchName + "] was created");
            String user = getUser(payload, "actor");
            String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = repo.has("scm") ? repo.getString("scm") : "git";

            return probe.triggerMatchingJobs(user, url, scm, payload.toString(), branchName, request.getHeader("X-Hub-Signature"), bodyBytes);
        } else {
            if (payload.has("repository")) {
                JSONObject repo = payload.getJSONObject("repository");
                LOGGER.log(Level.FINER, "Received commit hook notification for {0}", repo);

                String user = getUser(payload, "actor");
                String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
                String scm = repo.has("scm") ? repo.getString("scm") : "git";

                return probe.triggerMatchingJobs(user, url, scm, payload.toString(), null, request.getHeader("X-Hub-Signature"), bodyBytes);
            } else if (payload.has("scm")) {
                LOGGER.log(Level.FINER, "Received commit hook notification for hg: {0}", payload);
                String user = getUser(payload, "owner");
                String url = payload.getJSONObject("links").getJSONObject("html").getString("href");
                String scm = payload.has("scm") ? payload.getString("scm") : "hg";

                return probe.triggerMatchingJobs(user, url, scm, payload.toString(), null, request.getHeader("X-Hub-Signature"), bodyBytes);
            }
       }
       return BitbucketWebhookResult.IGNORED;
    }

    private String getBranchName(JSONObject payload) {
        if (payload.has("push")) {
            LOGGER.log(Level.FINER, "found [push] in payload");
            JSONObject jsonObjectPush = payload.getJSONObject("push");
            if (jsonObjectPush.has("changes")) {
                LOGGER.log(Level.FINER, "found [push/changes] in payload");
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
            LOGGER.log(Level.FINER, "found [push] in payload");
            JSONObject jsonObjectPush = payload.getJSONObject("push");
            if (jsonObjectPush.has("changes")){
                LOGGER.log(Level.FINER, "found [push/changes] in payload");
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
    private BitbucketWebhookResult processWebhookPayloadBitBucketServer(JSONObject payload, HttpServletRequest request, byte[] bodyBytes) {
        JSONObject repo = payload.getJSONObject("repository");
        String user = getUser(payload, "actor");
        String url = "";
        if (repo.getJSONObject("links").getJSONArray("self").size() != 0) {
            try {
                URL pushHref = new URL(repo.getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href"));
                url = pushHref.toString().replaceFirst("projects.*", repo.getString("fullName").toLowerCase());
                String scm = repo.has("scmId") ? repo.getString("scmId") : "git";
                return probe.triggerMatchingJobs(user, url, scm, payload.toString(), null, request.getHeader("X-Hub-Signature"), bodyBytes);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, String.format("URL %s is malformed", url), e);
            }
        }
        return BitbucketWebhookResult.IGNORED;
    }

    private BitbucketWebhookResult processPostServicePayload(JSONObject payload, HttpServletRequest request, byte[] bodyBytes) {
        JSONObject repo = payload.getJSONObject("repository");
        LOGGER.log(Level.FINER, "Received commit hook notification for {0}", repo);

        String user = payload.getString("user");
        String url = payload.getString("canon_url") + repo.getString("absolute_url");
        String scm = repo.getString("scm");

        return probe.triggerMatchingJobs(user, url, scm, payload.toString(), null, request.getHeader("X-Hub-Signature"), bodyBytes);
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayloadProcessor.class.getName());

}
