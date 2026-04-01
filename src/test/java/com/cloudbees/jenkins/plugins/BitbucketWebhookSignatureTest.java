package com.cloudbees.jenkins.plugins;

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.util.Secret;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithJenkins
class BitbucketWebhookSignatureTest {

    private static final String PAYLOAD = "{\"push\":{\"changes\":[]},\"repository\":{\"links\":{\"html\":{\"href\":\"https://bitbucket.org/test/repo\"}}},\"actor\":{\"nickname\":\"test-user\"}}";

    @Test
    void shouldAcceptUnsignedWebhookWhenSecretNotConfigured(JenkinsRule jenkins) throws Exception {
        createJob(jenkins, null);

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            WebResponse response = submitWebhook(webClient, PAYLOAD, null);

            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    void shouldRejectSignedWebhookWhenJobHasNoSecretConfigured(JenkinsRule jenkins) throws Exception {
        createJob(jenkins, null);

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            WebResponse response = submitWebhook(
                    webClient,
                    PAYLOAD,
                    BitbucketWebhookSignatureValidator.createSignatureHeader("sha256", "super-secret", PAYLOAD)
            );

            assertEquals(403, response.getStatusCode());
        }
    }

    @Test
    void shouldAcceptSignedWebhookWhenSecretConfigured(JenkinsRule jenkins) throws Exception {
        createJob(jenkins, "It's a Secret to Everybody");

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            WebResponse response = submitWebhook(
                    webClient,
                    PAYLOAD,
                    BitbucketWebhookSignatureValidator.createSignatureHeader("sha256", "It's a Secret to Everybody", PAYLOAD)
            );

            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    void shouldRejectMissingSignatureWhenSecretConfigured(JenkinsRule jenkins) throws Exception {
        createJob(jenkins, "super-secret");

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            WebResponse response = submitWebhook(webClient, PAYLOAD, null);

            assertEquals(403, response.getStatusCode());
        }
    }

    @Test
    void shouldRejectInvalidSignatureWhenSecretConfigured(JenkinsRule jenkins) throws Exception {
        createJob(jenkins, "super-secret");

        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            WebResponse response = submitWebhook(webClient, PAYLOAD, "sha256=deadbeef");

            assertEquals(403, response.getStatusCode());
        }
    }

    @Test
    void shouldRejectWebhookWhenNoMatchingJobExists(JenkinsRule jenkins) throws Exception {
        try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            WebResponse response = submitWebhook(webClient, PAYLOAD, null);

            assertEquals(200, response.getStatusCode());
        }
    }

    private WebResponse submitWebhook(JenkinsRule.WebClient webClient, String payload, String signature) throws Exception {
        WebRequest request = new WebRequest(new URL(webClient.getContextPath() + "bitbucket-hook/"), HttpMethod.POST);
        request.setAdditionalHeader("Content-Type", "application/json");
        request.setAdditionalHeader("X-Event-Key", "repo:push");
        request.setAdditionalHeader("User-Agent", "Bitbucket-Webhooks/2.0");
        if (signature != null) {
            request.setAdditionalHeader("X-Hub-Signature", signature);
        }
        request.setRequestBody(payload);
        return webClient.getPage(request).getWebResponse();
    }

    private void createJob(JenkinsRule jenkins, String secret) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("bitbucket-job");
        project.setScm(new GitSCM("https://bitbucket.org/test/repo"));

        BitBucketTrigger trigger = new BitBucketTrigger();
        if (secret != null) {
            trigger.setWebhookSecret(Secret.fromString(secret));
        }
        project.addTrigger(trigger);
        trigger.start(project, true);
        project.save();
    }
}
