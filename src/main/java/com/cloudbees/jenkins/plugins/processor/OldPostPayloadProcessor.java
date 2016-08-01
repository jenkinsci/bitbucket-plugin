package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;
import com.cloudbees.jenkins.plugins.payload.OldPostBitbucketPayload;
import net.sf.json.JSONObject;

/**
 * Old post payload processor
 * @since August 1, 2016
 * @version 2.0
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
