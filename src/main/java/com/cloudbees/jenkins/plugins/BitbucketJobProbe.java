/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;
import hudson.security.ACL;

import java.net.URISyntaxException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.triggers.Trigger;
import jenkins.model.Jenkins;

import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import com.google.common.base.Objects;

public class BitbucketJobProbe {
    public void triggetMatchingJobs(BitbucketEvent bitbucketEvent, BitbucketPayload bitbucketPayload) {
        if("git".equals(bitbucketPayload.getScm()) || "hg".equals(bitbucketPayload.getScm())) {
            SecurityContext old = Jenkins.getInstance().getACL().impersonate(ACL.SYSTEM);

            try {
                URIish remote = new URIish(bitbucketPayload.getScmUrl());

                for (Job<?,?> job : Jenkins.getInstance().getAllItems(Job.class)) {
                    LOGGER.log(Level.FINE, "Considering candidate job {0}", job.getName());

                    BitBucketTrigger bitbucketTrigger = getBitBucketTrigger(job);
                    if (bitbucketTrigger != null) {
                        LOGGER.log(Level.FINE, "Considering to poke {0}", job.getFullDisplayName());

                        SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);

                        List<SCM> scmTriggered = new ArrayList<SCM>();

                        for (SCM scmTrigger : item.getSCMs()) {
                            if (match(scmTrigger, remote) && !hasBeenTriggered(scmTriggered, scmTrigger)) {
                                scmTriggered.add(scmTrigger);

                                bitbucketTrigger.onPost(bitbucketEvent, bitbucketPayload);
                            } else {
                                LOGGER.log(Level.FINE, "{0} SCM doesn't match remote repo {1}", new Object[]{job.getName(), remote});
                            }
                        }
                    }
                }
            } catch (URISyntaxException e) {
                LOGGER.log(Level.WARNING, "Invalid repository URL {0}", bitbucketPayload.getScm());
            } finally {
                SecurityContextHolder.setContext(old);
            }

        } else {
            throw new UnsupportedOperationException("Unsupported SCM type " + bitbucketPayload.getScm());
        }
    }

    private BitBucketTrigger getBitBucketTrigger(Job<?, ?> job) {
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
            for (Trigger trigger : pJob.getTriggers().values()) {
                if (trigger instanceof BitBucketTrigger) {
                    return (BitBucketTrigger) trigger;
                }
            }
        }

        return null;
    }

    private boolean hasBeenTriggered(List<SCM> scmTriggered, SCM scmTrigger) {
        for (SCM scm : scmTriggered) {
            if (scm.equals(scmTrigger)) {
                LOGGER.log(Level.FINEST, "Has been triggered {0}", scmTrigger.getType());
                return true;
            }
        }
        return false;
    }

    private boolean match(SCM scm, URIish url) {
        if (scm instanceof GitSCM) {
            for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
                for (URIish urIish : remoteConfig.getURIs()) {
                    LOGGER.log(Level.FINEST, "Comparing {0} and {1} looselyMatches", new Object[]{urIish.toString(), url.toString()});
                    if (GitStatus.looselyMatches(urIish, url)) {
                        LOGGER.log(Level.FINEST, "{0} and {1} looselyMatched successfully", new Object[]{urIish.toString(), url.toString()});
                        return true;
                    }
                }
            }
        } else if (scm instanceof MercurialSCM) {
            try {
                URI hgUri = new URI(((MercurialSCM) scm).getSource());
                String remote = url.toString();
                if (looselyMatches(hgUri, remote)) {
                    return true;
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
            }
        }
        return false;
    }

    private boolean looselyMatches(URI notifyUri, String repository) {
        boolean result = false;
        try {
            URI repositoryUri = new URI(repository);
            result = Objects.equal(notifyUri.getHost(), repositoryUri.getHost())
            && Objects.equal(notifyUri.getPath(), repositoryUri.getPath())
            && Objects.equal(notifyUri.getQuery(), repositoryUri.getQuery());
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Could not parse repository uri: {0}, {1}", new Object[]{repository, ex});
        }
        return result;
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketJobProbe.class.getName());

}
