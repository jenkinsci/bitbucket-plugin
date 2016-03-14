package com.cloudbees.jenkins.plugins.filter;

import com.cloudbees.jenkins.plugins.BitBucketPushCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import com.cloudbees.jenkins.plugins.processor.BitbucketPayloadProcessor;
import hudson.model.AbstractDescribableImpl;

/**
 * Created by Shyri Villar on 14/03/2016.
 */
public abstract class BitbucketTriggerFilter extends AbstractDescribableImpl<BitbucketTriggerFilter> {


    public abstract boolean shouldScheduleJob(BitBucketPayload bitbucketPayload);
    public abstract BitBucketPushCause getCause();
//    public static DescriptorExtensionList<BitbucketTriggerConfig, BitbucketTriggerFilterDescriptor> all() {
//        return Jenkins.getInstance().getDescriptorList(BitbucketTriggerConfig.class);
//    }
//
//    @Override
//    public Descriptor<BitbucketTriggerConfig> getDescriptor() {
//        return Jenkins.getInstance().getDescriptor(getClass());
//    }
}
