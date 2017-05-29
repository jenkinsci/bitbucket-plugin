package com.cloudbees.jenkins.plugins;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class CrumbExclusionTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldNotRequireACrumbForTheBitbucketHookUrl() throws IOException, SAXException {
        JenkinsRule.WebClient webClient = jenkins.createWebClient();
        WebRequest wrs = new WebRequest(new URL(webClient.getContextPath() + "bitbucket-hook"),
                HttpMethod.POST);
        WebResponse resp = webClient.getPage(wrs).getWebResponse();

        assertEquals(resp.getStatusCode(), 200);
    }
}
