# Please read before usage
I forked the main repo and fixed the pull-request-created feature. We do use this i production so it works BUT use at own risk.
This repo will only exist until the develop branch is released to the master branch in the main repo

bitbucket-plugin
================

[![Build Status](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/badge/icon)](https://jenkins.ci.cloudbees.com/job/plugins/job/bitbucket-plugin/)

See details on [wiki](https://wiki.jenkins-ci.org/display/JENKINS/BitBucket+Plugin)

# Pipeline script
Pipeline code for building on pull-request create event. It merge from source to target in the PR.

```
properties([
    pipelineTriggers([
        [
            $class: 'BitBucketTrigger',
            triggers : [
                [
                    $class: 'PullRequestTriggerFilter',
                    actionFilter: [
                        $class: 'PullRequestCreatedActionFilter'
                    ]
                ]
            ]
        ]
    ])
])
node {
        def sourceBranch = ""
        def targetBranch = ""
        try{
            sourceBranch = "${BITBUCKET_SOURCE_BRANCH}";
            targetBranch = "${BITBUCKET_TARGET_BRANCH}";
        }catch(e){}

        if(sourceBranch == ""){
            sourceBranch = 'development'
        }

        if(targetBranch == ""){
            targetBranch = 'master'
        }

        checkout changelog: true, poll: true, scm: [
            $class: 'GitSCM',
            branches: [
                [name: '*/'+sourceBranch]
            ],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                 [
                    $class: 'PreBuildMerge',
                    options: [
                        fastForwardMode: 'FF',
                        mergeRemote: 'origin',
                        mergeStrategy: 'recursive',
                        mergeTarget: ''+targetBranch
                    ]
                ]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                [
                    url: 'https://[user]@bitbucket.org/[org]/[repo].git']
                ]
            ]


        echo 'Some build steps'

}
```

# Build snapshot with docker

```
# Mac and Linux users
docker run -it --rm -v $(pwd):/usr/src/mymaven -v mvn-data:/root/.m2 -w /usr/src/mymaven maven:alpine bash

# Windows users
docker run -it --rm -v /c/Users/[your profile folder name fx. bitbucket]:/usr/src/mymaven -v mvn-data:/root/.m2 -w /usr/src/mymaven maven:alpine bash

#Build the snapshot
mvn package -DskipTests
```