package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;

/**
 * The factory for the {@link BitbucketPayloadProcessor}
 * @since August 1, 2016
 * @version 2.0
 */
public class BitbucketPayloadProcessorFactory {
    /**
     * Creates a {@link BitbucketPayloadProcessor} based on the {@link BitbucketEvent}
     *
     * @return {@link BitbucketPayloadProcessor}
     */
    public BitbucketPayloadProcessor create(BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName())) {
            return new RepositoryPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        } else if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName())) {
            return new PullRequestPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        }

        throw new UnsupportedOperationException("Bitbucket event " + bitbucketEvent.getName() + " not supported");
    }

    public BitbucketPayloadProcessor create(BitbucketJobProbe probe, BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName())) {
            return new RepositoryPayloadProcessor(probe, bitbucketEvent);
        } else if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName())) {
            return new PullRequestPayloadProcessor(probe, bitbucketEvent);
        }

        return null;
    }

    /**
     * Creates a {@link BitbucketPayloadProcessor} based on the {@link BitbucketEvent}
     *
     * @return {@link BitbucketPayloadProcessor}
     */
    public BitbucketPayloadProcessor createOldProcessor(BitbucketEvent bitbucketEvent) {
        return new OldPostPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
    }
}