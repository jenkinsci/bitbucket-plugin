package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import jenkins.branch.MultiBranchProject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Extension
public class BitbucketEnvironmentContributor extends EnvironmentContributor {
    private static final Logger LOGGER = Logger.getLogger(BitbucketEnvironmentContributor.class.getName());

    @Override
    public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(r, envs, listener);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void buildEnvironmentFor(@NonNull Job j, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(j, envs, listener);
        ItemGroup parent = j.getParent();
        AtomicReference<BitBucketMultibranchTrigger> bitBucketMultibranchTrigger = new AtomicReference<>(null);
        if ( parent instanceof MultiBranchProject){
            ((MultiBranchProject<?, ?>) parent).getTriggers().forEach((triggerDescriptor, trigger) -> {
                if ( trigger instanceof BitBucketMultibranchTrigger){
                    bitBucketMultibranchTrigger.set((BitBucketMultibranchTrigger) trigger);
                }
            });
        }

        if ( bitBucketMultibranchTrigger.get() != null){
            if ( bitBucketMultibranchTrigger.get().getPayload() == null){
                LOGGER.finest("BITBUCKET_PAYLOAD is null, ignoring");
            } else {
                envs.put("BITBUCKET_PAYLOAD", bitBucketMultibranchTrigger.get().getPayload());
            }
        }
    }
}
