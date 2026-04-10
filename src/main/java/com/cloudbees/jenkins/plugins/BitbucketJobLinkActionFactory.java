package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.model.TransientActionFactory;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Extension
public class BitbucketJobLinkActionFactory extends TransientActionFactory<Job> {

    @FunctionalInterface
    interface SCMHeadByItemProvider {
        SCMHead findHead(Item item);
    }

    @FunctionalInterface
    interface SCMSourceByItemProvider {
        SCMSource findSource(Item item);
    }

    private final BitbucketLinkUtils linkUtils;
    private final SCMHeadByItemProvider headProvider;
    private final SCMSourceByItemProvider sourceProvider;

    public BitbucketJobLinkActionFactory() {
        this(new BitbucketLinkUtils(), SCMHead.HeadByItem::findHead, SCMSource.SourceByItem::findSource);
    }

    BitbucketJobLinkActionFactory(BitbucketLinkUtils linkUtils,
                                  SCMHeadByItemProvider headProvider,
                                  SCMSourceByItemProvider sourceProvider) {
        this.linkUtils = linkUtils;
        this.headProvider = headProvider;
        this.sourceProvider = sourceProvider;
    }

    @Override
    public Collection<? extends Action> createFor(Job target) {
        if (target instanceof WorkflowJob) {
            WorkflowJob workflowJob = (WorkflowJob) target;
            Optional<BitbucketExternalLink> branchLink = createBranchLink(workflowJob);
            if (branchLink.isPresent()) {
                return Collections.singletonList(branchLink.get());
            }
            if (workflowJob.getDefinition() instanceof CpsScmFlowDefinition) {
                SCM scm = ((CpsScmFlowDefinition) workflowJob.getDefinition()).getScm();
                return linkUtils.createRepoLink(scm)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            }
        }

        if (target instanceof TopLevelItem && target instanceof hudson.model.Project) {
            SCM scm = ((hudson.model.Project<?, ?>) target).getScm();
            if (scm instanceof GitSCM) {
                return linkUtils.createRepoLink(scm)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            }
        }

        return Collections.emptyList();
    }

    private Optional<BitbucketExternalLink> createBranchLink(WorkflowJob workflowJob) {
        if (!(workflowJob.getParent() instanceof WorkflowMultiBranchProject)) {
            return Optional.empty();
        }
        SCMSource source = sourceProvider.findSource(workflowJob);
        SCMHead head = headProvider.findHead(workflowJob);
        if (source == null || head == null) {
            return Optional.empty();
        }
        return linkUtils.createBranchLink(source, head.getName());
    }

    @Override
    public Class<Job> type() {
        return Job.class;
    }
}
