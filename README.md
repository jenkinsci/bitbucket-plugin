bitbucket-plugin
================

[![Build Status](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/badge/icon)](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/)

See details on [wiki](https://wiki.jenkins-ci.org/display/JENKINS/BitBucket+Plugin)

# Job DSL
The plugin supports the following dsl extension to enable bitbucket pushes to trigger a build:
```freeStyleJob('test-job') {
  triggers{
    bitbucketPush()
  }
}```