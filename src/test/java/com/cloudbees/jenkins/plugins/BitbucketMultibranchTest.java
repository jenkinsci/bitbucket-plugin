package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import jenkins.branch.BranchSource;
import jenkins.branch.BranchIndexingCause;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.junit.jupiter.WithGitSampleRepo;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SuppressWarnings({"rawtypes", "ResultOfMethodCallIgnored"})
@WithJenkins
@WithGitSampleRepo
class BitbucketMultibranchTest {

    private JenkinsRule jenkinsRule;

    private GitSampleRepoRule sampleRepo;

    @BeforeEach
    void setUp(JenkinsRule rule, GitSampleRepoRule repo) {
        jenkinsRule = rule;
        sampleRepo = repo;
    }

    @Test
    void testWorkflowMultiBranchProject() throws Exception{
        BitbucketMultibranchRunListener instance =
                jenkinsRule.jenkins.getExtensionList(hudson.model.listeners.RunListener.class).get(BitbucketMultibranchRunListener.class);
        assertNotNull(instance);


        // Initialize a Git repository
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "pipeline { agent any;  triggers { bitbucketPush }  stages { stage('Build') { steps { echo 'Building...' } } } }");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=Initial commit");

        // Create a new WorkflowMultiBranchProject
        WorkflowMultiBranchProject workflowMultiBranchProject = jenkinsRule.jenkins.createProject(WorkflowMultiBranchProject.class, "my-project");

        // Add a GitSCMSource to the project
        GitSCMSource gitSource = new GitSCMSource(sampleRepo.toString());
        gitSource.getTraits().add(new BranchDiscoveryTrait());

        workflowMultiBranchProject.getSourcesList().add(new BranchSource(gitSource));

        // Schedule and find a branch project
        WorkflowJob job = scheduleAndFindBranchProject(workflowMultiBranchProject);


        // Get the last build and perform assertions
        WorkflowRun build = job.getLastBuild();
        assertNotNull(build);
        assertEquals(1, build.getNumber());
        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains("Branch indexing", build);
        assertNull(build.getAction(BitBucketPayload.class));
    }

    @Test
    @Issue("JENKINS-75752")
    void webhookPayloadIsInjectedOnlyIntoWebhookTriggeredBranchBuilds() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "pipeline { agent any; stages { stage('Build') { steps { echo 'Building...' } } } }");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=Initial commit");

        WorkflowMultiBranchProject workflowMultiBranchProject =
                jenkinsRule.jenkins.createProject(WorkflowMultiBranchProject.class, "payload-project");

        GitSCMSource gitSource = new GitSCMSource(sampleRepo.toString());
        gitSource.getTraits().add(new BranchDiscoveryTrait());
        workflowMultiBranchProject.getSourcesList().add(new BranchSource(gitSource));

        BitBucketMultibranchTrigger trigger = new BitBucketMultibranchTrigger();
        workflowMultiBranchProject.addTrigger(trigger);
        trigger.start(workflowMultiBranchProject, true);
        workflowMultiBranchProject.save();

        WorkflowJob job = scheduleAndFindBranchProject(workflowMultiBranchProject);
        WorkflowRun initialBuild = job.getLastBuild();
        assertNotNull(initialBuild);
        assertNull(initialBuild.getAction(BitBucketPayload.class));

        sampleRepo.write("change.txt", "new change");
        sampleRepo.git("add", "change.txt");
        sampleRepo.git("commit", "--message=Webhook commit");

        String payload = "{\"from\":\"webhook\"}";
        BitbucketWebhookResult result =
                new BitbucketJobProbe().triggerMatchingJobs("tzachs", sampleRepo.toString(), "git", payload, null, null, null);

        assertEquals(BitbucketWebhookResult.TRIGGERED, result);
        jenkinsRule.waitUntilNoActivity();

        WorkflowRun webhookBuild = job.getLastBuild();
        assertNotNull(webhookBuild);
        assertTrue(webhookBuild.getNumber() > initialBuild.getNumber());
        assertNotNull(webhookBuild.getCause(BranchIndexingCause.class));
        assertEquals(payload, webhookBuild.getEnvironment(TaskListener.NULL).get("BITBUCKET_PAYLOAD"));
        assertEquals(payload, webhookBuild.getAction(BitBucketPayload.class).getPayload());

        WorkflowRun manualBuild = jenkinsRule.buildAndAssertSuccess(job);
        assertNull(manualBuild.getCause(BranchIndexingCause.class));
        assertNull(manualBuild.getAction(BitBucketPayload.class));
        assertNull(manualBuild.getEnvironment(TaskListener.NULL).get("BITBUCKET_PAYLOAD"));
    }

    private static class Bla extends CauseAction implements EnvironmentContributingAction {

        @Override
        public void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars env) {
            EnvironmentContributingAction.super.buildEnvironment(run, env);
            env.put("BITBUCKET_PAYLOAD", "checking_payload");
        }
    }

    private WorkflowJob scheduleAndFindBranchProject(WorkflowMultiBranchProject workflowMultiBranchProject) throws Exception {

        // Schedule indexing and wait for completion
        Queue.Item queueItem = workflowMultiBranchProject.scheduleBuild2(0,
                new CauseAction(new BitBucketPushCause("tzachs")), new Bla());

        Queue.Executable executable = Objects.requireNonNull(queueItem).getFuture().get();
        if (executable instanceof MultiBranchProject.BranchIndexing branchIndexing) {
            String multiBranchLog = getLog(branchIndexing.getLogText());
            jenkinsRule.assertStringContains(multiBranchLog, "Starting branch indexing");
            jenkinsRule.assertStringContains(multiBranchLog, "Started by BitBucket push by tzachs");
        }
        jenkinsRule.waitUntilNoActivity();

        return workflowMultiBranchProject.getItem("master");

    }

    private String getLog(AnnotatedLargeText logText) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        logText.writeLogTo(0, baos);

        return baos.toString(StandardCharsets.UTF_8);
    }
}
