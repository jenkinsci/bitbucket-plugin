package com.cloudbees.jenkins.plugins;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;

@Ignore
public class CrumbExclusionTest {
	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	@Test
	public void shouldNotRequireACrumbForTheBitbucketHookUrl() throws IOException, SAXException {
		JenkinsRule.WebClient webClient = jenkins.createWebClient();
		WebRequestSettings wrs = new WebRequestSettings(new URL(webClient.getContextPath() + "bitbucket-hook"), HttpMethod.POST);
		WebResponse resp = webClient.getPage(wrs).getWebResponse();

		assertEquals(resp.getStatusCode(), 200);
	}
}
