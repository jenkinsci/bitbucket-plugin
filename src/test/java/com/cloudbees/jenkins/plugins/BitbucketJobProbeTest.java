package com.cloudbees.jenkins.plugins;

import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author pfrank
 */
public class BitbucketJobProbeTest {
  private BitbucketJobProbe bitbucketJobProbe;

  @Before
  public void setUp() {
    bitbucketJobProbe = new BitbucketJobProbe();
  }

  @Test
  public void sanitizePath() throws URISyntaxException{
    //GIVEN
    final URIish urIish = new URIish("http://example.com/scm/foo/bar");

    //WHEN
    final URIish retVal = bitbucketJobProbe.sanitizePath(urIish);

    //THEN
    assertEquals("/foo/bar", retVal.getPath());
  }

  @Test
  public void sanitzePathScmMiddle() throws URISyntaxException{
    //GIVEN
    final URIish urIish = new URIish("http://example.com/stash/scm/bar");

    //WHEN
    final URIish retVal = bitbucketJobProbe.sanitizePath(urIish);

    //THEN
    assertEquals( "/stash/bar", retVal.getPath());
  }
}