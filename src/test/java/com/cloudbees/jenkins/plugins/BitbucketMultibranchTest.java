package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.*;


@SuppressWarnings({"rawtypes", "ResultOfMethodCallIgnored"})
public class BitbucketMultibranchTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();


    @Test
    public void testWorkflowMultiBranchProject() throws Exception{
        BitbucketEnvironmentContributor instance =
                jenkinsRule.jenkins.getExtensionList(EnvironmentContributor.class).get(BitbucketEnvironmentContributor.class);
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
        if ( executable instanceof MultiBranchProject.BranchIndexing){
            MultiBranchProject.BranchIndexing branchIndexing = (MultiBranchProject.BranchIndexing) executable;
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
