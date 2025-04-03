package com.cloudbees.jenkins.plugins;

import hudson.model.JobProperty;
import hudson.triggers.Trigger;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty;
import org.jenkinsci.plugins.workflow.multibranch.JobPropertyStep;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * @author Allan Burdajewicz
 */
@WithJenkins
class BitbucketTriggerTest {

    @Test
    @Issue("JENKINS-44309")
    void symbolAnnotationBitbucketTrigger(JenkinsRule j) throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("properties([pipelineTriggers([bitbucketPush()])])\n", false));
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));
        assertFalse(p.getTriggers().isEmpty());
        Trigger<?> trigger = p.getTriggersJobProperty().getTriggers().get(0);
        assertNotNull(trigger);
        assertInstanceOf(BitBucketTrigger.class, trigger);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void configRoundTripBitbucketTrigger(JenkinsRule j) throws Exception {
        PipelineTriggersJobProperty triggersProperty = new PipelineTriggersJobProperty(null);
        triggersProperty.addTrigger(new BitBucketTrigger());
        List<JobProperty> properties = Collections.singletonList(triggersProperty);
        new SnippetizerTester(j).assertRoundTrip(new JobPropertyStep(properties), "properties([pipelineTriggers([bitbucketPush()])])");
    }
}
