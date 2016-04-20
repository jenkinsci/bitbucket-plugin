package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import com.cloudbees.jenkins.plugins.payload.OldPostBitbucketPayload;
import net.sf.json.JSONObject;

/**
 * Created by Shyri Villar on 15/03/2016.
 */
public class OldPostPayloadProcessor extends BitbucketPayloadProcessor {
    public OldPostPayloadProcessor(BitbucketJobProbe jobProbe, BitbucketEvent bitbucketEvent) {
        super(jobProbe, bitbucketEvent);
    }

    @Override
    public void processPayload(JSONObject payload) {
//        LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

        BitbucketPayload bitbucketPayload = new OldPostBitbucketPayload(payload);

        jobProbe.triggetMatchingJobs(bitbucketEvent, bitbucketPayload);
    }


}
