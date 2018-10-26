package com.cloudbees.jenkins.plugins;

import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import hudson.plugins.git.GitSCM;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketJobProbeTest {

    @Mock
    private GitSCM scmMock;
    @Mock
    private RemoteConfig remoteConfigMock;

    private BitbucketJobProbe bitbucketJobProbe;


    @Before
    public void setup() {
        bitbucketJobProbe = new BitbucketJobProbe();

        List<RemoteConfig> configs = new ArrayList<>();
        configs.add(remoteConfigMock);

        when(scmMock.getRepositories()).thenReturn(configs);
    }

    @Test
    public void test_whenMatchUrlContainsScm() {

        assertTrue(testMatchWithUrls("https://bitbucket_server/scm/project/repo.git", "https://bitbucket_server/project/repo.git"));
        assertTrue(testMatchWithUrls("https://bitbucket/server/scm/project/repo.git", "https://bitbucket/server/project/repo.git"));
    }

    private boolean testMatchWithUrls(String jobUrlStr, String payloadUrlStr) {
        try {
            URIish jobUri = new URIish(jobUrlStr);
            List<URIish> uris = new ArrayList<>();
            uris.add(jobUri);
            when(remoteConfigMock.getURIs()).thenReturn(uris);

            URIish payloadUri = new URIish(payloadUrlStr);
            return bitbucketJobProbe.match(scmMock, payloadUri);

        } catch (URISyntaxException e) {
            System.out.println(e.getStackTrace());
            return false;
        }
    }
}
