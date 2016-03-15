package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import com.cloudbees.jenkins.plugins.payload.PullRequestPayload;
import net.sf.json.JSONObject;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class PullRequestPayloadProcessor extends BitbucketPayloadProcessor {
    public PullRequestPayloadProcessor(BitbucketJobProbe jobProbe, BitbucketEvent bitbucketEvent) {
        super(jobProbe, bitbucketEvent);
    }

    @Override
    public void processPayload(JSONObject payload) {
        if (payload.has("repository")) {
            BitbucketPayload bitbucketPayload = buildPayloadForJobs(payload);
            jobProbe.triggetMatchingJobs(bitbucketEvent, bitbucketPayload);

        } else if (payload.has("scm")) {
//            probe.triggerMatchingJobs(user, url, scm, payload.toString()); TODO
        }
    }

    private BitbucketPayload buildPayloadForJobs(JSONObject payload) {
        return new PullRequestPayload(payload);
    }

}
