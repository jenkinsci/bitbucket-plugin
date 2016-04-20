package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;

/**
 * Created by isvillar on 11/03/2016.
 */
public class BitbucketPayloadProcessorFactory {
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

    public BitbucketPayloadProcessor createOldProcessor(BitbucketEvent bitbucketEvent) {
        return new OldPostPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
    }
}