package com.cloudbees.jenkins.plugins;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

public class BitbucketJobProbe {

    public void triggerMatchingJobs(String user, String url, String scm) {
        if ("git".equals(scm)) {
            SecurityContext old = Jenkins.getInstance().getACL().impersonate(ACL.SYSTEM);
            try {
                URIish remote = new URIish(url);
                for (AbstractProject<?,?> job : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                    LOGGER.info("considering candidate job " + job.getName());
                    BitBucketTrigger trigger = job.getTrigger(BitBucketTrigger.class);
                    if (trigger!=null) {
                        if (match(job.getScm(), remote)) {
                        	trigger.onPost(user);
                        } else LOGGER.info("job SCM doesn't match remote repo");
                    } else LOGGER.info("job hasn't BitBucketTrigger set");
                }
            } catch (URISyntaxException e) {
                LOGGER.warning("invalid repository URL " + url);
            } finally {
                SecurityContextHolder.setContext(old);
            }

        } else {
            // TODO hg
            throw new UnsupportedOperationException("unsupported SCM type " + scm);
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

    private static final Logger LOGGER = Logger.getLogger(BitbucketJobProbe.class.getName());

}
