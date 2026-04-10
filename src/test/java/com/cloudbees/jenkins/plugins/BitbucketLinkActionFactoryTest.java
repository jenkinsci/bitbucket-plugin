package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import jenkins.branch.BranchSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class BitbucketLinkActionFactoryTest {

    @Test
    void multibranchProjectsGetBrowseRepositoryLink(JenkinsRule j) throws Exception {
        WorkflowMultiBranchProject project = j.jenkins.createProject(WorkflowMultiBranchProject.class, "mbp");
        BitbucketSCMSource source = new BitbucketSCMSource("PRJ", "repo");
        source.setServerUrl("https://stash.example.com/bitbucket");
        project.getSourcesList().add(new BranchSource(source));

        BitbucketExternalLink action = project.getAction(BitbucketExternalLink.class);

        assertNotNull(action);
        assertEquals(BitbucketLinkUtils.REPOSITORY_LINK_NAME, action.getDisplayName());
        assertEquals("https://stash.example.com/bitbucket/projects/PRJ/repos/repo", action.getUrlName());
    }
}
