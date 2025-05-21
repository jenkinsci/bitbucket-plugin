package com.cloudbees.jenkins.plugins;

import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.plugins.mercurial.MercurialSCMSource;
import hudson.scm.SCM;
import hudson.security.ACL;

import java.net.URISyntaxException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;


import jenkins.model.Jenkins;

import jenkins.model.ParameterizedJobMixIn;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.triggers.SCMTriggerItem;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import com.google.common.base.Objects;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;

public class BitbucketJobProbe {

    private boolean isBranchPluginAvailable = false;

    public BitbucketJobProbe() {
        if (Jenkins.get().getPlugin("cloudbees-bitbucket-branch-source") != null) {
            LOGGER.log(Level.FINEST, "Bitbucket branch source available");
            isBranchPluginAvailable = true;
        }
    }

    public void triggerMatchingJobs(String user, String url, String scm, String payload) {
        triggerMatchingJobs(user, url, scm, payload, null);
    }

    public void triggerMatchingJobs(String user, String url, String scm, String payload, String branchName) {
        if ("git".equals(scm) || "hg".equals(scm)) {
            SecurityContext old = Jenkins.getInstance().getACL().impersonate2(ACL.SYSTEM2);
            try {
                URIish remote = new URIish(url);
                for (Job<?, ?> job : Jenkins.getInstance().getAllItems(Job.class)) {
                    BitBucketTrigger bTrigger = null;
                    LOGGER.log(Level.FINE, "Considering candidate job [{0}]", job.getName());

                    if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
                        ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
                        for (Object trigger : pJob.getTriggers().values()) {
                            if (trigger instanceof BitBucketTrigger) {
                                bTrigger = (BitBucketTrigger) trigger;
                                LOGGER.log(Level.FINE, "Job [{0}] has BitBucketTrigger", job.getName());
                                break;
                            } else if (trigger instanceof BitBucketMultibranchTrigger) {
                                LOGGER.fine("Trigger is BitBucketMultibranchTrigger");
                            }
                        }
                    } else {
                        LOGGER.finest("job [" + job.getName() + "] is not ParameterizedJobMixIn.ParameterizedJob. [" + job.getClass().getSimpleName() + "]");
                    }
                    if (bTrigger == null) {
                        LOGGER.log(Level.FINE, "[{0}] hasn't BitBucketTrigger set", job.getName());
                    } else {
                        LOGGER.log(Level.FINE, "Considering to poke {0}", job.getFullDisplayName());
                        SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
                        if (item == null) {
                            LOGGER.log(Level.FINER, "item is null");
                        } else {
                            List<SCM> scmTriggered = new ArrayList<>();
                            if (item.getSCMs().isEmpty()) {
                                LOGGER.log(Level.FINE, "No SCM configuration was found!");
                            }
                            for (SCM scmTrigger : item.getSCMs()) {
                                if (match(scmTrigger, remote, bTrigger.getOverrideUrl()) && !hasBeenTriggered(scmTriggered, scmTrigger)) {
                                    LOGGER.log(Level.FINER, "Triggering BitBucket job {0}", job.getFullDisplayName());
                                    scmTriggered.add(scmTrigger);
                                    bTrigger.onPost(user, payload, branchName);
                                } else {
                                    LOGGER.log(Level.FINEST, String.format("[%s] SCM doesn't match remote repo [%s]", job.getName(), remote));
                                }
                            }
                        }
                    }
                }
                LOGGER.log(Level.FINE, "Now checking SCMSourceOwners/multiBranchProjects");
                for (SCMSourceOwner scmSourceOwner : Jenkins.getInstance().getAllItems(SCMSourceOwner.class)) {
                    LOGGER.log(Level.FINE, "Considering candidate scmSourceOwner {0}", scmSourceOwner.getFullDisplayName());
                    List<SCMSource> scmSources = scmSourceOwner.getSCMSources();
                    for (SCMSource scmSource : scmSources) {
                        LOGGER.log(Level.FINER, "Considering candidate scmSource {0}", scmSource);
                        if (match(scmSource, remote)) {
                            if (scmSourceOwner instanceof WorkflowMultiBranchProject) {
                                LOGGER.finest("scmSourceOwner [" + scmSourceOwner.getName() + "] is of type WorkflowMultiBranchProject");
                                WorkflowMultiBranchProject workflowMultiBranchProject  = (WorkflowMultiBranchProject) scmSourceOwner;
                                AtomicReference<BitBucketMultibranchTrigger> bitBucketMultibranchTrigger = new AtomicReference<>(null);
                                if (workflowMultiBranchProject.getTriggers().isEmpty()) {
                                    LOGGER.finest("No triggers found");
                                } else {
                                    workflowMultiBranchProject.getTriggers().forEach(((triggerDescriptor, trigger) -> {
                                        if (trigger instanceof BitBucketMultibranchTrigger) {
                                            LOGGER.finest("Found BitBucketMultibranchTrigger type");
                                            bitBucketMultibranchTrigger.set((BitBucketMultibranchTrigger) trigger);
                                        }
                                    }));
                                }
                                if (bitBucketMultibranchTrigger.get() == null) {
                                    scmSourceOwner.onSCMSourceUpdated(scmSource);
                                } else {
                                    if (workflowMultiBranchProject.isBuildable()) {
                                        bitBucketMultibranchTrigger.get().setPayload(payload);
                                        BitBucketPushCause bitBucketPushCause = new BitBucketPushCause(user);
                                        workflowMultiBranchProject.scheduleBuild2(0, new CauseAction(bitBucketPushCause));
                                    } else {
                                        LOGGER.finest("workflowMultiBranchProject is not builtable");
                                    }
                                }
                            } else {
                                scmSourceOwner.onSCMSourceUpdated(scmSource);
                            }
                        } else if (scmSourceOwner instanceof WorkflowMultiBranchProject) {
                            LOGGER.finest("scmSourceOwner [" + scmSourceOwner.getName() + "] is of type WorkflowMultiBranchProject");
                            WorkflowMultiBranchProject workflowMultiBranchProject = (WorkflowMultiBranchProject) scmSourceOwner;
                            if (workflowMultiBranchProject.getTriggers().isEmpty()) {
                                LOGGER.finest("No triggers found");
                            } else {
                                workflowMultiBranchProject.getTriggers().forEach(((triggerDescriptor, trigger) -> {
                                    if (trigger instanceof BitBucketMultibranchTrigger) {
                                        LOGGER.finest("Found BitBucketMultibranchTrigger type");
                                        BitBucketMultibranchTrigger bitBucketMultibranchTrigger = (BitBucketMultibranchTrigger) trigger;
                                        if (bitBucketMultibranchTrigger.getOverrideUrl() == null || bitBucketMultibranchTrigger.getOverrideUrl().isEmpty()) {
                                            LOGGER.finest("Ignoring empty overrideUrl");
                                        } else {
                                            LOGGER.fine("Found override URL [" + bitBucketMultibranchTrigger.getOverrideUrl() + "]");
                                            LOGGER.log(Level.FINE, "Trying to match {0} ", remote + "<-->" + bitBucketMultibranchTrigger.getOverrideUrl());
                                            if (bitBucketMultibranchTrigger.getOverrideUrl().equalsIgnoreCase(remote.toString())) {
                                                LOGGER.info(String.format("Triggering BitBucket scmSourceOwner [%s] by overrideUrl [%s]",scmSourceOwner.getName(), bitBucketMultibranchTrigger.getOverrideUrl()));
                                                scmSourceOwner.onSCMSourceUpdated(scmSource);
                                            }
                                        }
                                    } else {
                                        LOGGER.finest("Found BitBucketMultibranchTrigger type");
                                    }
                                }));
                            }
                        } else {

                            LOGGER.log(Level.FINE, String.format("SCM [%s] doesn't match remote repo [%s]", scmSourceOwner.getFullDisplayName(), remote));
                        }
                    }
                }
            } catch (URISyntaxException e) {
                LOGGER.log(Level.WARNING, "Invalid repository URL {0}", url);
            } finally {
                SecurityContextHolder.setContext(old);
            }

        } else {
            throw new UnsupportedOperationException("Unsupported SCM type " + scm);
        }
    }

    private boolean hasBeenTriggered(List<SCM> scmTriggered, SCM scmTrigger) {
        for (SCM scm : scmTriggered) {
            if (scm.equals(scmTrigger)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(SCM scm, URIish url, String overrideUrl) {
        if (scm instanceof GitSCM) {
            LOGGER.log(Level.FINE, "SCM is instance of GitSCM");
            for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
                for (URIish urIish : remoteConfig.getURIs()) {
                    // needed cause the ssh and https URI differs in Bitbucket Server.
                    if (urIish.getPath().startsWith("/scm")) {
                        urIish = urIish.setPath(urIish.getPath().substring(4));
                    }

                    // needed because bitbucket self hosted does not transfer any host information
                    if (StringUtils.isEmpty(url.getHost())) {
                        urIish = urIish.setHost(url.getHost());
                    }

                    LOGGER.log(Level.FINE, "Trying to match {0} ", urIish.toString() + "<-->" + url);
                    if (GitStatus.looselyMatches(urIish, url)) {
                        return true;
                    } else if (overrideUrl != null && !overrideUrl.isEmpty()) {
                        LOGGER.log(Level.FINE, "Trying to match using override Repository URL {0} ", overrideUrl + "<-->" + url);
                        return overrideUrl.contentEquals(url.toString());
                    }
                }
            }
        } else if (scm instanceof MercurialSCM) {
            LOGGER.log(Level.FINEST, "SCM is instance of MercurialSCM");
            try {
                URI hgUri = new URI(((MercurialSCM) scm).getSource());
                String remote = url.toString();
                if (looselyMatches(hgUri, remote)) {
                    return true;
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
            }
        } else {
            LOGGER.log(Level.FINEST, "SCM is instance of [" + scm.getClass().getSimpleName() + "] which is not supported");
        }

        return false;
    }

    private boolean match(SCMSource scm, URIish url) {
        if (scm instanceof GitSCMSource || (isBranchPluginAvailable && scm instanceof BitbucketSCMSource)) {
            String gitRemote;
            if (scm instanceof GitSCMSource) {
                LOGGER.log(Level.FINEST, "SCMSource is GitSCMSource");
                gitRemote = ((GitSCMSource) scm).getRemote();
            } else if (isBranchPluginAvailable) {
                LOGGER.log(Level.FINEST, "SCMSource is BitbucketSCMSource");
                gitRemote = ((BitbucketSCMSource) scm).getServerUrl() + "/" +
                            ((BitbucketSCMSource) scm).getRepoOwner() + "/" +
                            ((BitbucketSCMSource) scm).getRepository();
            } else {
                return false;
            }
            URIish urIish;
            LOGGER.log(Level.FINEST, "SCMSource remote is " + gitRemote);
            try {
                urIish = new URIish(gitRemote);
            } catch (URISyntaxException e) {
                LOGGER.log(Level.SEVERE, "Could not parse gitRemote: " + gitRemote, e);
                return false;
            }
            // needed cause the ssh and https URI differs in Bitbucket Server.
            if (urIish.getPath().startsWith("/scm")) {
                urIish = urIish.setPath(urIish.getPath().substring(4));
            }

            // needed because bitbucket self hosted does not transfer any host information
            if (StringUtils.isEmpty(url.getHost())) {
                urIish = urIish.setHost(url.getHost());
            }

            LOGGER.log(Level.FINE, "Trying to match {0} ", urIish.toString() + "<-->" + url);
            return GitStatus.looselyMatches(urIish, url);
        } else if (scm instanceof MercurialSCMSource) {
            LOGGER.log(Level.FINEST, "SCMSource is MercurialSCMSource");
            try {
                URI hgUri = new URI(((MercurialSCMSource) scm).getSource());
                String remote = url.toString();
                if (looselyMatches(hgUri, remote)) {
                    return true;
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.SEVERE, "Could not parse jobSource uri: {0} ", ex);
            }
        } else {
            LOGGER.log(Level.FINEST, "SCMSource is [" + scm.getClass().getSimpleName() + "] which is not supported");
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
