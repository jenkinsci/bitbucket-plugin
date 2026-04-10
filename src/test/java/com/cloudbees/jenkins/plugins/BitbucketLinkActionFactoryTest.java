package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import jenkins.branch.BranchSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class BitbucketLinkActionFactoryTest {

    private static class PullRequestHead extends SCMHead implements ChangeRequestSCMHead {
        PullRequestHead(String name) {
            super(name);
        }

        @Override
        public String getId() {
            return "1";
        }

        @Override
        public SCMHead getTarget() {
            return new SCMHead("main") {};
        }
    }

    private static class TagHead extends SCMHead implements TagSCMHead {
        TagHead(String name) {
            super(name);
        }

        @Override
        public long getTimestamp() {
            return 0;
        }
    }

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

    @Test
    void branchLinksAreSuppressedForPullRequestsAndTags() {
        assertEquals(false, BitbucketJobLinkActionFactory.supportsBranchLink(new PullRequestHead("PR-1")));
        assertEquals(false, BitbucketJobLinkActionFactory.supportsBranchLink(new TagHead("v1.0.0")));
        assertEquals(true, BitbucketJobLinkActionFactory.supportsBranchLink(new SCMHead("main") {}));
    }
}
