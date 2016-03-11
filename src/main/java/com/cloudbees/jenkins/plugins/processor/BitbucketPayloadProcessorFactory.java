package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;

/**
 * Created by isvillar on 11/03/2016.
 */
public class BitbucketPayloadProcessorFactory {
    public BitbucketPayloadProcessor create(BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENTS.REPOSITORY.equals(bitbucketEvent.getKey())) {
            return new RepositoryPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        } else if(BitbucketEvent.EVENTS.PULL_REQUEST.equals(bitbucketEvent.getKey())) {
            return new PullRequestPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        }

        throw new RuntimeException(); //TODO
    }

    public BitbucketPayloadProcessor create(BitbucketJobProbe probe, BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENTS.REPOSITORY.equals(bitbucketEvent.getKey())) {
            return new RepositoryPayloadProcessor(probe, bitbucketEvent);
        } else if(BitbucketEvent.EVENTS.PULL_REQUEST.equals(bitbucketEvent.getKey())) {
            return new PullRequestPayloadProcessor(probe, bitbucketEvent);
        } else {
//            TODO
//            LOGGER.log(Level.INFO, "Processing old POST service payload");
//            processPostServicePayload(payload);
        }

        return null;
    }
}