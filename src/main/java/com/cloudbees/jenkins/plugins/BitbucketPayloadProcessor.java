package com.cloudbees.jenkins.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
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
        String eventKey = request.getHeader("x-event-key");
        if ("Bitbucket-Webhooks/2.0".equals(request.getHeader("user-agent"))) {
            if ("repo:push".equals(eventKey)) {
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayload(payload);
            } else {
                LOGGER.log(Level.FINE, "Ignored webhook with event key {0}", eventKey);
            }
        } else if (payload.has("actor") && payload.has("repository")) {
            if ("repo:push".equals(eventKey) || "repo:refs_changed".equals(eventKey)) {
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayloadBitBucketServer(payload);
            } else {
                LOGGER.log(Level.FINE, "Ignored webhook with event key {0}", eventKey);
            }
        } else {
            LOGGER.log(Level.INFO, "Processing old POST service payload");
            processPostServicePayload(payload);
        }
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
        JSONObject actor = payload.getJSONObject("actor");
        String user = null;
        if (actor.has("username")) {
            user = actor.getString("username");
        } else if (actor.has("slug")) {
            user = actor.getString("slug");
        }
        String url = "";
        JSONObject links = repo.getJSONObject("links");
        if (links.has("clone")) {
            JSONArray clones = links.getJSONArray("clone");
            for (int i = 0; i < clones.size(); i++) {
                String scm = repo.has("scmId") ? repo.getString("scmId") : "git";
                url = clones.getJSONObject(i).getString("href");
                probe.triggerMatchingJobs(user, url, scm, payload.toString());
            }

        } else if (links.getJSONArray("self").size() != 0) {
            try {
                URL pushHref = new URL(links.getJSONArray("self").getJSONObject(0).getString("href"));
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
