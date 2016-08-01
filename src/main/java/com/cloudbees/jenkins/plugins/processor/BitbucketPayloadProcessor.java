package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import net.sf.json.JSONObject;

/**
 * Process the BitBucket payload
 * @since August 1, 2016
 * @version 2.0
 */
public abstract class BitbucketPayloadProcessor {
    protected final BitbucketJobProbe jobProbe;
    protected final BitbucketEvent bitbucketEvent;

    public BitbucketPayloadProcessor(BitbucketJobProbe jobProbe, BitbucketEvent bitbucketEvent) {
        this.jobProbe = jobProbe;
        this.bitbucketEvent = bitbucketEvent;
    }

    /**
     * Payload processor
     */
    public abstract void processPayload(JSONObject payload);
}
