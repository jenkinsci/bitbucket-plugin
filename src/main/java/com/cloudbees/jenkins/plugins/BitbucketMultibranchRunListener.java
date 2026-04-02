package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.branch.BranchIndexingCause;
import jenkins.branch.MultiBranchProject;

import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BitbucketMultibranchRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        if (run.getAction(BitBucketPayload.class) != null) {
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

        BitBucketPayload payload = indexing.getAction(BitBucketPayload.class);
        if (payload == null) {
            LOGGER.log(Level.FINEST, "No Bitbucket payload action found on active indexing for multibranch project [{0}]", multiBranchProject.getFullName());
            return;
        }

        LOGGER.log(Level.FINEST, "Attaching Bitbucket payload to run [{0}] from multibranch indexing", run.getExternalizableId());
        run.addAction(new BitBucketPayload(payload.getPayload()));
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketMultibranchRunListener.class.getName());
}
