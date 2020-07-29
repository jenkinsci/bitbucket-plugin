Bitbucket Plugin for Jenkins
============================

[![Build Status](https://ci.jenkins.io/job/Plugins/job/bitbucket-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/bitbucket-plugin/job/master/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/bitbucket.svg)](https://plugins.jenkins.io/bitbucket/)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/bitbucket.svg?label=changelog)](https://github.com/jenkinsci/bitbucket-plugin/releases/latest/)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/bitbucket.svg?color=blue)](https://plugins.jenkins.io/bitbucket/)

## About

Bitbucket plugin is designed to offer integration between Bitbucket and Jenkins.

It exposes a single URI endpoint that you can add as a WebHook within each Bitbucket project you wish to integrate with. This single endpoint receives a full data payload from Bitbucket upon push (see their documentation), triggering compatible jobs to build based on changed repository/branch.

Since 1.1.5 Bitbucket automatically injects the payload received by Bitbucket into the build. You can catch the payload to process it accordingly through the environmental variable $BITBUCKET\_PAYLOAD.

  

## Bitbucket Cloud usage

Configure your Bitbucket repository with a [Webhook](https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html), using URL JENKINS\_URL/bitbucket-hook/ (no need for credentials but do remember the trailing slash).

![](docs/images/Screen_Shot_2018-09-14_at_3.19.12_PM.png)


The older-style HTTP POSTs from Bitbucket are also supported but deprecated.

![](docs/images/Capture_d’écran_2014-02-22_à_19.20.45.png)

On each push, the plugin:

1.  Scans Jenkins for all jobs with "Build when a change is pushed to Bitbucket" option enabled
2.  For each job matched:
    1.  If the job's SCM (git) URL "loosely matches" that of the git repository listed inside the Bitbucket-provided payload, AND
    2.  If the job's SCM (git) detects that the remote repository has changes, THEN
    3.  A full build of the job will be queued

The "loose matching" is based on the host name and paths of the projects matching.



## Bitbucket server usage

Since the version 1.1.7 of the Bitbucket plugin works against Bitbucket server. For this plugin to work against Bitbucket server you must: 

1.  Install [Post Webhooks for Bitbucket](https://marketplace.atlassian.com/plugins/nl.topicus.bitbucket.bitbucket-webhooks/server/overview) at Bitbucket side \[the plugin is free\]
2.  At repository level, delete the webhook in case it exists

![](docs/images/Screen_Shot_2017-12-05_at_15.14.27.png)

3. Create a Post-WebHook, which is different from WebHook and enable on push.

![](docs/images/Screen_Shot_2017-12-05_at_15.15.17.png)

  

After this, you are all set-up

## Job DSL

The current supported dsl is as follows:

``` syntaxhighlighter-pre
freeStyleJob('test-job') {
  triggers{
    bitbucketPush()
  }
}
```

**Changelog**

#### 1.1.15  (29. July 2020)

- Fixed javadoc error preventing from releasing 1.1.14

#### 1.1.14 ((Not released)

- Fixed CVE-2020-5529
- Updated to jenkins version to 2.204.1 

#### 1.1.13 (26. July 2020)

-  See [PR-75](https://github.com/jenkinsci/bitbucket-plugin/pull/75), supporting repos that end with .git

#### 1.1.12 (Not released)

#### 1.1.11 (27. August 2019)

-   Added possibility to process trigger from bitbucket server default webhooks [PR-63](https://github.com/jenkinsci/bitbucket-plugin/pull/63) 

#### 1.1.10 (4. July 2019)

-   Update job-dsl dependency to 1.66.
    See [PR-58](https://github.com/jenkinsci/bitbucket-plugin/pull/58) 

#### 1.1.9 (1. Jun 2019)

-   Address [Bitbucket API change](https://developer.atlassian.com/cloud/bitbucket/bitbucket-api-changes-gdpr/?_ga=2.164415676.2088283489.1559219877-1322535506.1557927444): 
    [JENKINS-57787](https://issues.jenkins-ci.org/browse/JENKINS-57787)
    - Getting issue details... STATUS

#### 1.1.7 (6. Dec 2017)

-   Add Jenkins ci integration
-   [JENKINS-28877](https://issues.jenkins-ci.org/browse/JENKINS-28877) :
    Add integration for Bitbucket server

#### 1.1.6 (2. Nov 2017)

-   fix 
    [JENKINS-44309](https://issues.jenkins-ci.org/browse/JENKINS-44309) 
    Add support for Symbol

#### 1.1.5 (26. Jan 2016)

-   fix
    [JENKINS-32372](https://issues.jenkins-ci.org/browse/JENKINS-32372)
    Inject the Payload into the build through $BITBUCKET\_PAYLOAD

#### 1.1.4 (28. Dec 2015)

-   Add
    [JENKINS-31185](https://issues.jenkins-ci.org/browse/JENKINS-31185) hg
    support
-   Add [Job DSL extension](https://github.com/jenkinsci/bitbucket-plugin/pull/24)
-   fix
    [JENKINS-26234](https://issues.jenkins-ci.org/browse/JENKINS-26234)
    CSRF support

#### 1.1.3 (16. Oct 2015)

-   fix
    [JENKINS-29096](https://issues.jenkins-ci.org/browse/JENKINS-29096)
    Advice users when they don't use the right hook url - last \`/\`
-   fix
    [JENKINS-30985](https://issues.jenkins-ci.org/browse/JENKINS-30985)
    Jobs with the same git repository defined several times in the scm should be triggered only once

#### 1.1.2 (4. August 2015)

-   fix
    [JENKINS-28882](https://issues.jenkins-ci.org/browse/JENKINS-28882)
    Workflow support for BitBucket trigger

#### 1.1.1 (9. July 2015)

-   Allow Webhooks 2.0

#### 1.1.0 (10. March 2015)

-   fix
    [JENKINS-24999](https://issues.jenkins-ci.org/browse/JENKINS-24999)
    Build triggered by SCM change without activating trigger in the job configuration
-   fix
    [JENKINS-26413](https://issues.jenkins-ci.org/browse/JENKINS-26413)
    BitBucket trigger doesn't need to Initialize LogFile
-   fix
    [JENKINS-26489](https://issues.jenkins-ci.org/browse/JENKINS-26489)
    Action report for the bitbucket polling log on web UI
-   fix
    [JENKINS-26805](https://issues.jenkins-ci.org/browse/JENKINS-26805)
    Job is not triggered after merging a branch

#### 1.0

-   initial implementation
