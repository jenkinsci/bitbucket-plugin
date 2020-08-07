package com.cloudbees.jenkins.plugins.extensions.dsl;

import com.cloudbees.jenkins.plugins.BitBucketTrigger;
import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.triggers.TriggerContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class BitbucketHookJobDslExtension extends ContextExtensionPoint {
    @DslExtensionMethod(context = TriggerContext.class)
    public Object bitbucketPush(Runnable closure) {
        return new BitBucketTrigger("");
    }
}


