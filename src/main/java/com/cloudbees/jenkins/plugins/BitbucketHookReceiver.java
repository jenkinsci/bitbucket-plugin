package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BitbucketHookReceiver implements UnprotectedRootAction {


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
        processPayload(payload);
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
    private void processPayload(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        String user = payload.getString("user");
        String url = payload.getString("canon_url") + repo.getString("absolute_url");
        LOGGER.info("Received commit hook notification for "+repo);

        JSONArray commits = payload.getJSONArray("commits");
        int last = commits.size() - 1;
        String sha1 = commits.getJSONObject(last).getString("raw_node");
        String branch = commits.getJSONObject(last).getString("branch");

        String scm = repo.getString("scm");
        if ("git".equals(scm)) {
            SecurityContext old = Jenkins.getInstance().getACL().impersonate(ACL.SYSTEM);
            try {
                URIish remote = new URIish(url);
                for (AbstractProject<?,?> job : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                    LOGGER.info("Considering candidate job " + job.getName());
                    BitBucketTrigger trigger = job.getTrigger(BitBucketTrigger.class);
                    if (trigger!=null) {
                        if (match(job.getScm(), remote)) {
                        	trigger.onPost(user);
                        } else LOGGER.info("Job " + job.getName() + " SCM doesn't match remote repo");
                    } else LOGGER.info("Job "+ job.getName() + " hasn't BitBucketTrigger set");
                }
            } catch (URISyntaxException e) {
                LOGGER.warning("Invalid repository URL " + url);
            } finally {
                SecurityContextHolder.setContext(old);
            }

        } else {
            // TODO hg
            throw new UnsupportedOperationException("Unsupported SCM type " + scm);
        }
    }

    private boolean match(SCM scm, URIish url) {
        if (scm instanceof GitSCM) {
            for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
                for (URIish urIish : remoteConfig.getURIs()) {
                    if (GitStatus.looselyMatches(urIish, url))
                        return true;
                }
            }
        }
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketHookReceiver.class.getName());

}
