package com.cloudbees.jenkins.plugins;

import hudson.model.JobProperty;
import hudson.triggers.Trigger;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty;
import org.jenkinsci.plugins.workflow.multibranch.JobPropertyStep;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.List;

/**
 * @author Allan Burdajewicz
 */
public class BitbucketTriggerTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test
    @Issue("JENKINS-44309")
    public void symbolAnnotationBitbucketTrigger() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("properties([pipelineTriggers([bitbucketPush()])])\n"));
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));
        Assert.assertFalse(p.getTriggers().isEmpty());
        Trigger trigger = p.getTriggersJobProperty().getTriggers().get(0);
        Assert.assertNotNull(trigger);
        Assert.assertTrue(trigger instanceof BitBucketTrigger);
    }

    @SuppressWarnings("rawtypes")
    @Test public void configRoundTripBitbucketTrigger() throws Exception {
        PipelineTriggersJobProperty triggersProperty = new PipelineTriggersJobProperty(null);
        triggersProperty.addTrigger(new BitBucketTrigger());
        List<JobProperty> properties = Collections.<JobProperty>singletonList(triggersProperty);
        new SnippetizerTester(j).assertRoundTrip(new JobPropertyStep(properties), "properties([pipelineTriggers([bitbucketPush()])])");
    }
}
