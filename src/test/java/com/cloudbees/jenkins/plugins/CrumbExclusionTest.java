package com.cloudbees.jenkins.plugins;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URL;

@WithJenkins
class CrumbExclusionTest {

    @Test
    void shouldNotRequireACrumbForTheBitbucketHookUrl(JenkinsRule jenkins) throws Exception {
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            WebRequest wrs = new WebRequest(new URL(webClient.getContextPath() + "bitbucket-hook"),
                    HttpMethod.POST);
            WebResponse resp = webClient.getPage(wrs).getWebResponse();

            assertEquals(200, resp.getStatusCode());
        }
    }
}
