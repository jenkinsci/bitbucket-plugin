package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.Action;
import jenkins.model.TransientActionFactory;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.Collection;
import java.util.Collections;

@Extension
public class BitbucketMultibranchLinkActionFactory extends TransientActionFactory<WorkflowMultiBranchProject> {

    private final BitbucketLinkUtils linkUtils;

    public BitbucketMultibranchLinkActionFactory() {
        this(new BitbucketLinkUtils());
    }

    BitbucketMultibranchLinkActionFactory(BitbucketLinkUtils linkUtils) {
        this.linkUtils = linkUtils;
    }

    @Override
    public Collection<? extends Action> createFor(WorkflowMultiBranchProject target) {
        for (SCMSource source : target.getSCMSources()) {
            if (linkUtils.createRepoLink(source).isPresent()) {
                return Collections.singletonList(linkUtils.createRepoLink(source).get());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Class<WorkflowMultiBranchProject> type() {
        return WorkflowMultiBranchProject.class;
    }
}
