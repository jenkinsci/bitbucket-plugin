package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import net.sf.json.JSONObject;

/**
 * Created by isvillar on 11/03/2016.
 */
public class RepositoryPayloadProcessor extends BitbucketPayloadProcessor{
    public RepositoryPayloadProcessor(BitbucketJobProbe probe, BitbucketEvent bitbucketEvent) {
        super(probe, bitbucketEvent);
    }

    @Override
    public void processPayload(JSONObject payload) {
// TODO
    }
}
