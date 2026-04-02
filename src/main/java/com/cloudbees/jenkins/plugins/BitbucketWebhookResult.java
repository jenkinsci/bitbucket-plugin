package com.cloudbees.jenkins.plugins;

enum BitbucketWebhookResult {
    TRIGGERED,
    NO_MATCH,
    INVALID_SIGNATURE,
    IGNORED
}
