package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;
import net.sf.json.JSONObject;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public abstract class BitbucketPayloadProcessor {
    protected final BitbucketJobProbe jobProbe;
    protected final BitbucketEvent bitbucketEvent;

    public BitbucketPayloadProcessor(BitbucketJobProbe jobProbe, BitbucketEvent bitbucketEvent) {
        this.jobProbe = jobProbe;
        this.bitbucketEvent = bitbucketEvent;
    }

    public abstract void processPayload(JSONObject payload);
}
