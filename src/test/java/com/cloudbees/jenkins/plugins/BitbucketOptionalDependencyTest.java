package com.cloudbees.jenkins.plugins;

import hudson.plugins.mercurial.MercurialSCMSource;
import jenkins.branch.BranchSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.RealJenkinsExtension;

import static org.junit.jupiter.api.Assertions.assertNull;

class BitbucketOptionalDependencyTest {

    @RegisterExtension
    private static final RealJenkinsExtension REAL_JENKINS = new RealJenkinsExtension()
            .omitPlugins("cloudbees-bitbucket-branch-source")
            .withTimeout(120);

    @Test
    void multibranchActionsWorkWithoutBitbucketBranchSource() throws Throwable {
        REAL_JENKINS.then(BitbucketOptionalDependencyTest::verifyMultibranchActions);
    }

    private static void verifyMultibranchActions(JenkinsRule j) throws Exception {
        assertNull(j.jenkins.getPlugin("cloudbees-bitbucket-branch-source"));

        WorkflowMultiBranchProject project =
                j.jenkins.createProject(WorkflowMultiBranchProject.class, "mbp-mercurial");
        project.getSourcesList().add(new BranchSource(
                new MercurialSCMSource("https://example.invalid/repository")));

        assertNull(project.getAction(BitbucketExternalLink.class));
    }
}
