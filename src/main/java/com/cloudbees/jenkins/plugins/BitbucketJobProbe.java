package com.cloudbees.jenkins.plugins;

import hudson.model.Hudson;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import hudson.triggers.Trigger;
import jenkins.model.Jenkins;

import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
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
                for (Job<?,?> job : Hudson.getInstance().getAllItems(Job.class)) {
                    BitBucketTrigger bTrigger = null;
                    LOGGER.info("considering candidate job " + job.getName());
                    ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;

                    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
                        for (Trigger trigger : pJob.getTriggers().values()) {
                            if (trigger instanceof BitBucketTrigger) {
                                bTrigger = (BitBucketTrigger) trigger;
                                break;
                            }
                        }
                    }
                    if (bTrigger != null) {
                        LOGGER.info("Considering to poke " + job.getFullDisplayName());
                        SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);

                        for (SCM scmTrigger : item.getSCMs()) {
                            if (match(scmTrigger, remote)) {
                                bTrigger.onPost(user);
                            } else LOGGER.info("job SCM doesn't match remote repo");
                        }
                    } else {
                        LOGGER.info("job hasn't BitBucketTrigger set");
                    }
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
