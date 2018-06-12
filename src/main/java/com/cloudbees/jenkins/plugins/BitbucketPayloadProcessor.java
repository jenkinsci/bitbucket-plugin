package com.cloudbees.jenkins.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
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
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayload(payload);
            }
        } else if (payload.has("actor") && payload.has("repository") && payload.getJSONObject("repository").has("links")) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayloadBitBucketServer(payload);
            }
        } else if (payload.has("actor")) {
        	// we assume that the passed hook was from bitbucket server https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html
        	LOGGER.log(Level.INFO, "Processing webhook for self-hosted bitbucket instance");
        	processWebhookPayloadBitBucketSelfHosted(payload);
        } else {
            LOGGER.log(Level.INFO, "Processing old POST service payload");
            processPostServicePayload(payload);
        }
    }

    /**
     * parses the payload from self hosted bitbucket instance which uses the default webhooks implementation.
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
        String url = repo.getJSONObject("project").getString("key").toLowerCase() + "/" + repo.getString("name");

        // always use git no other repo type supported on self hosted solution
        String scm = "git";
        probe.triggerMatchingJobs(user, url, scm, payload.toString());
		
	}

	private void processWebhookPayload(JSONObject payload) {
        if (payload.has("repository")) {
            JSONObject repo = payload.getJSONObject("repository");
            LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

            String user = payload.getJSONObject("actor").getString("username");
            String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = repo.has("scm") ? repo.getString("scm") : "git";

            probe.triggerMatchingJobs(user, url, scm, payload.toString());
        } else if (payload.has("scm")) {
            LOGGER.log(Level.INFO, "Received commit hook notification for hg: {0}", payload);
            String user = payload.getJSONObject("owner").getString("username");
            String url = payload.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = payload.has("scm") ? payload.getString("scm") : "hg";

            probe.triggerMatchingJobs(user, url, scm, payload.toString());
        }

    }

    /**
     * Payload processor for BitBucket server. The plugin Post Webhooks for Bitbucket
     * https://marketplace.atlassian.com/plugins/nl.topicus.bitbucket.bitbucket-webhooks/server/overview
     * should be installed and configured
     *
     * @param payload
     */
    private void processWebhookPayloadBitBucketServer(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        String user = payload.getJSONObject("actor").getString("username");
        String url = "";
        if (repo.getJSONObject("links").getJSONArray("self").size() != 0) {
            try {
                URL pushHref = new URL(repo.getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href"));
                url = pushHref.toString().replaceFirst(new String("projects.*"), new String(repo.getString("fullName").toLowerCase()));
                String scm = repo.has("scmId") ? repo.getString("scmId") : "git";
                probe.triggerMatchingJobs(user, url, scm, payload.toString());
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, String.format("URL %s is malformed", url), e);
            }
        }
    }

/*
{
    "canon_url": "https://bitbucket.org",
    "commits": [
        {
            "author": "marcus",
            "branch": "master",
            "files": [
                {
                    "file": "somefile.py",
                    "type": "modified"
                }
            ],
            "message": "Added some more things to somefile.py\n",
            "node": "620ade18607a",
            "parents": [
                "702c70160afc"
            ],
            "raw_author": "Marcus Bertrand <marcus@somedomain.com>",
            "raw_node": "620ade18607ac42d872b568bb92acaa9a28620e9",
            "revision": null,
            "size": -1,
            "timestamp": "2012-05-30 05:58:56",
            "utctimestamp": "2012-05-30 03:58:56+00:00"
        }
    ],
    "repository": {
        "absolute_url": "/marcus/project-x/",
        "fork": false,
        "is_private": true,
        "name": "Project X",
        "owner": "marcus",
        "scm": "git",
        "slug": "project-x",
        "website": "https://atlassian.com/"
    },
    "user": "marcus"
}
*/
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
