bitbucket-plugin
================

[![Build Status](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/badge/icon)](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/)

See details on [wiki](https://wiki.jenkins-ci.org/display/JENKINS/BitBucket+Plugin)

# Job DSL
The plugin supports the following dsl extension to enable bitbucket pushes to trigger a build:

```
freeStyleJob('test-job') {
  triggers{
    bitbucketPush()
  }
}
```

# --skip-ci
The plugin supports the --skip-ci flag in the commit message to avoid triggering a job build. The supported flags are "--skip-ci", "[ci skip]" and "[skip ci]" .

It will only check for valid flags in the message of the first commit.

For example, the following commit messages will NOT trigger a job build:

```
"Merged by Jenkins CI --skip-ci"

"Merged by Jenkins CI [ci skip]"

"Merged by Jenkins CI [skip ci]"
```