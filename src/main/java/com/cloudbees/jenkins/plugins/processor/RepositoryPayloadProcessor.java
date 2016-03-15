package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import com.cloudbees.jenkins.plugins.payload.PullRequestPayload;
import com.cloudbees.jenkins.plugins.payload.RepositoryPayload;
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
        if (payload.has("repository")) {
            BitBucketPayload bitBucketPayload = buildPayloadForJobs(payload);
            jobProbe.triggetMatchingJobs(bitbucketEvent, bitBucketPayload);

        } else if (payload.has("scm")) {
//            probe.triggerMatchingJobs(user, url, scm, payload.toString()); TODO
        }
    }

    private BitBucketPayload buildPayloadForJobs(JSONObject payload) {
        return new RepositoryPayload(payload);
    }
}
