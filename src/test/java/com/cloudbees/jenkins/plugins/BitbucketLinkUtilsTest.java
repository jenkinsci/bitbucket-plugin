package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitbucketLinkUtilsTest {

    private final BitbucketLinkUtils linkUtils = new BitbucketLinkUtils();

    @Test
    void parsesBitbucketServerHttpRemote() {
        BitbucketLinkUtils.BitbucketRemote remote = linkUtils
                .parseRemote("https://stash.example.com/bitbucket/scm/PRJ/repo.git")
                .orElseThrow();

        assertEquals("https://stash.example.com/bitbucket/projects/PRJ/repos/repo", remote.toRepositoryUrl());
        assertEquals(
                "https://stash.example.com/bitbucket/projects/PRJ/repos/repo/compare/commits?sourceBranch=refs%2Fheads%2Ffeature%2Ftest",
                remote.toBranchUrl("feature/test")
        );
    }

    @Test
    void parsesBitbucketServerSshRemote() {
        BitbucketLinkUtils.BitbucketRemote remote = linkUtils
                .parseRemote("ssh://git@stash.example.com:7999/bitbucket/scm/PRJ/repo.git")
                .orElseThrow();

        assertEquals("https://stash.example.com/bitbucket/projects/PRJ/repos/repo", remote.toRepositoryUrl());
    }

    @Test
    void parsesBitbucketCloudRemote() {
        BitbucketLinkUtils.BitbucketRemote remote = linkUtils
                .parseRemote("git@bitbucket.org:workspace/repo.git")
                .orElseThrow();

        assertEquals("https://bitbucket.org/workspace/repo", remote.toRepositoryUrl());
        assertEquals("https://bitbucket.org/workspace/repo/branch/feature%2Ftest", remote.toBranchUrl("feature/test"));
    }

    @Test
    void parsesBitbucketCloudSshRemoteFromIssueExample() {
        BitbucketLinkUtils.BitbucketRemote remote = linkUtils
                .parseRemote("git@bitbucket.org:tzachs/test.git")
                .orElseThrow();

        assertEquals("https://bitbucket.org/tzachs/test", remote.toRepositoryUrl());
        assertEquals("https://bitbucket.org/tzachs/test/branch/master", remote.toBranchUrl("master"));
    }

    @Test
    void parsesBitbucketServerScpStyleSshRemote() {
        BitbucketLinkUtils.BitbucketRemote remote = linkUtils
                .parseRemote("git@stash.example.com:PRJ/repo.git")
                .orElseThrow();

        assertEquals("https://stash.example.com/projects/PRJ/repos/repo", remote.toRepositoryUrl());
        assertEquals(
                "https://stash.example.com/projects/PRJ/repos/repo/compare/commits?sourceBranch=refs%2Fheads%2Ffeature%2Ftest",
                remote.toBranchUrl("feature/test")
        );
    }

    @Test
    void createsLinksFromBitbucketBranchSourceCoordinates() {
        BitbucketSCMSource source = new BitbucketSCMSource("PRJ", "repo");
        source.setServerUrl("https://stash.example.com/bitbucket");

        BitbucketExternalLink repoLink = linkUtils.createRepoLink(source).orElseThrow();
        BitbucketExternalLink branchLink = linkUtils.createBranchLink(source, "feature/test").orElseThrow();

        assertEquals(BitbucketLinkUtils.REPOSITORY_LINK_NAME, repoLink.getDisplayName());
        assertEquals("https://stash.example.com/bitbucket/projects/PRJ/repos/repo", repoLink.getUrlName());
        assertEquals(BitbucketLinkUtils.BRANCH_LINK_NAME, branchLink.getDisplayName());
        assertEquals(
                "https://stash.example.com/bitbucket/projects/PRJ/repos/repo/compare/commits?sourceBranch=refs%2Fheads%2Ffeature%2Ftest",
                branchLink.getUrlName()
        );
    }

    @Test
    void ignoresNonBitbucketRemote() {
        assertTrue(linkUtils.parseRemote("/tmp/local-repo").isEmpty());
        assertFalse(linkUtils.parseRemote("https://github.com/jenkinsci/bitbucket-plugin.git").isPresent());
    }
}
