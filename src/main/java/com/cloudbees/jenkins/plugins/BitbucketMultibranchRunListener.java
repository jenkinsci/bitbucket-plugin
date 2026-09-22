package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.branch.BranchIndexingCause;
import jenkins.branch.MultiBranchProject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BitbucketMultibranchRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        if (!run.getActions(BitBucketPayload.class).isEmpty()) {
            return;
        }

        BranchIndexingCause cause = run.getCause(BranchIndexingCause.class);
        if (cause == null) {
            return;
        }

        MultiBranchProject<?, ?> multiBranchProject = cause.getMultiBranchProject();
        if (multiBranchProject == null) {
            LOGGER.log(Level.FINEST, "Branch indexing cause did not resolve a multibranch project for run [{0}]", run.getExternalizableId());
            return;
        }

        MultiBranchProject.BranchIndexing<?, ?> indexing = multiBranchProject.getIndexing();
        if (indexing == null) {
            LOGGER.log(Level.FINEST, "No indexing computation found for multibranch project [{0}]", multiBranchProject.getFullName());
            return;
        }

        List<BitBucketPayload> payloads = indexing.getActions(BitBucketPayload.class);
        if (payloads.isEmpty()) {
            LOGGER.log(Level.FINEST, "No Bitbucket payload actions found on active indexing for multibranch project [{0}]", multiBranchProject.getFullName());
            return;
        }

        LOGGER.log(Level.FINEST, "Attaching Bitbucket payloads to run [{0}] from multibranch indexing", run.getExternalizableId());
        for (BitBucketPayload p: payloads) {
            run.addAction(new BitBucketPayload(cause, p.getPayload()));
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketMultibranchRunListener.class.getName());
}
