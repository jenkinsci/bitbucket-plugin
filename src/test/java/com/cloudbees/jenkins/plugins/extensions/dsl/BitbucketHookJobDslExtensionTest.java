/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.jenkins.plugins.extensions.dsl;

import com.cloudbees.jenkins.plugins.BitBucketTrigger;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestApprovedActionFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestCreatedActionFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestUpdatedActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryPushActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class BitbucketHookJobDslExtensionTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    public String getJobDSLTrigger(String trigger) {
        return "<project>\n"
        + "<builders>\n"
        + "<javaposse.jobdsl.plugin.ExecuteDslScripts>\n"
        + "<scriptText>\n"
        + trigger
        + "</scriptText>\n"
        + "</javaposse.jobdsl.plugin.ExecuteDslScripts>\n"
        + "</builders>\n"
        + "<publishers/>\n  <buildWrappers/>\n" + "</project>";
    }

    @Test
    public void testBitbucketPush() throws Exception {
        TopLevelItem topLevelItem = jenkinsRule.getInstance().createProjectFromXML("whatever",
                new ByteArrayInputStream((getJobDSLTrigger("freeStyleJob('test-job') { triggers{ bitbucketPush() } }\n")).getBytes()));
        if (topLevelItem instanceof FreeStyleProject) {
            FreeStyleProject freeStyleProject = (FreeStyleProject) topLevelItem;
            Future<FreeStyleBuild> build = freeStyleProject.scheduleBuild2(0, new Cause.UserCause());
            build.get(); //// let mock build finish
        }
        TopLevelItem whateverTopLevelItem =jenkinsRule.getInstance().getItem("test-job");
        if (whateverTopLevelItem instanceof FreeStyleProject) {
            FreeStyleProject whateverFreeStyleProject = (FreeStyleProject) whateverTopLevelItem;
            Map<TriggerDescriptor, Trigger<?>> triggers = whateverFreeStyleProject.getTriggers();
            assertEquals(triggers.size(), 1);
            for (Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet()) {
                if(entry.getValue() instanceof BitBucketTrigger) {
                    BitBucketTrigger bitBucketTrigger = (BitBucketTrigger) entry.getValue();
                    List<BitbucketTriggerFilter> bitbucketTriggerFilters = bitBucketTrigger.getTriggers();
                    assertEquals(bitbucketTriggerFilters.size(), 1);
                    for (BitbucketTriggerFilter bitbucketTriggerFilter : bitbucketTriggerFilters) {
                        assertEquals(bitbucketTriggerFilter instanceof RepositoryTriggerFilter, true);
                        if (bitbucketTriggerFilter instanceof  RepositoryTriggerFilter) {
                            RepositoryTriggerFilter repositoryTriggerFilter = (RepositoryTriggerFilter) bitbucketTriggerFilter;
                            assertEquals(repositoryTriggerFilter.getActionFilter() instanceof RepositoryPushActionFilter, true);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBitbucketRepositoryPushAction() throws Exception {
        TopLevelItem topLevelItem = jenkinsRule.getInstance().createProjectFromXML("whatever",
                new ByteArrayInputStream((getJobDSLTrigger("freeStyleJob('test-job') { triggers{ bitbucketRepositoryPushAction() } }\n")).getBytes()));
        if (topLevelItem instanceof FreeStyleProject) {
            FreeStyleProject freeStyleProject = (FreeStyleProject) topLevelItem;
            Future<FreeStyleBuild> build = freeStyleProject.scheduleBuild2(0, new Cause.UserCause());
            build.get(); //// let mock build finish
        }
        TopLevelItem whateverTopLevelItem =jenkinsRule.getInstance().getItem("test-job");
        if (whateverTopLevelItem instanceof FreeStyleProject) {
            FreeStyleProject whateverFreeStyleProject = (FreeStyleProject) whateverTopLevelItem;
            Map<TriggerDescriptor, Trigger<?>> triggers = whateverFreeStyleProject.getTriggers();
            assertEquals(triggers.size(), 1);
            for (Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet()) {
                if(entry.getValue() instanceof BitBucketTrigger) {
                    BitBucketTrigger bitBucketTrigger = (BitBucketTrigger) entry.getValue();
                    List<BitbucketTriggerFilter> bitbucketTriggerFilters = bitBucketTrigger.getTriggers();
                    assertEquals(bitbucketTriggerFilters.size(), 1);
                    for (BitbucketTriggerFilter bitbucketTriggerFilter : bitbucketTriggerFilters) {
                        assertEquals(bitbucketTriggerFilter instanceof RepositoryTriggerFilter, true);
                        if (bitbucketTriggerFilter instanceof  RepositoryTriggerFilter) {
                            RepositoryTriggerFilter repositoryTriggerFilter = (RepositoryTriggerFilter) bitbucketTriggerFilter;
                            assertEquals(repositoryTriggerFilter.getActionFilter() instanceof RepositoryPushActionFilter, true);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBitbucketPullRequestApprovedAction() throws Exception {
        TopLevelItem topLevelItem = jenkinsRule.getInstance().createProjectFromXML("whatever",
                new ByteArrayInputStream((getJobDSLTrigger("freeStyleJob('test-job') { triggers{ bitbucketPullRequestApprovedAction(false) } }\n")).getBytes()));
        if (topLevelItem instanceof FreeStyleProject) {
            FreeStyleProject freeStyleProject = (FreeStyleProject) topLevelItem;
            Future<FreeStyleBuild> build = freeStyleProject.scheduleBuild2(0, new Cause.UserCause());
            build.get(); //// let mock build finish
        }
        TopLevelItem whateverTopLevelItem =jenkinsRule.getInstance().getItem("test-job");
        if (whateverTopLevelItem instanceof FreeStyleProject) {
            FreeStyleProject whateverFreeStyleProject = (FreeStyleProject) whateverTopLevelItem;
            Map<TriggerDescriptor, Trigger<?>> triggers = whateverFreeStyleProject.getTriggers();
            assertEquals(triggers.size(), 1);
            for (Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet()) {
                if(entry.getValue() instanceof BitBucketTrigger) {
                    BitBucketTrigger bitBucketTrigger = (BitBucketTrigger) entry.getValue();
                    List<BitbucketTriggerFilter> bitbucketTriggerFilters = bitBucketTrigger.getTriggers();
                    assertEquals(bitbucketTriggerFilters.size(), 1);
                    for (BitbucketTriggerFilter bitbucketTriggerFilter : bitbucketTriggerFilters) {
                        assertEquals(bitbucketTriggerFilter instanceof PullRequestTriggerFilter, true);
                        if (bitbucketTriggerFilter instanceof PullRequestTriggerFilter) {
                            PullRequestTriggerFilter pullRequestTriggerFilter = (PullRequestTriggerFilter) bitbucketTriggerFilter;
                            assertEquals(pullRequestTriggerFilter.getActionFilter() instanceof PullRequestApprovedActionFilter, true);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBitbucketPullRequestCreatedAction() throws Exception {
        TopLevelItem topLevelItem = jenkinsRule.getInstance().createProjectFromXML("whatever",
                new ByteArrayInputStream((getJobDSLTrigger("freeStyleJob('test-job') { triggers{ bitbucketPullRequestCreatedAction(false) } }\n")).getBytes()));
        if (topLevelItem instanceof FreeStyleProject) {
            FreeStyleProject freeStyleProject = (FreeStyleProject) topLevelItem;
            Future<FreeStyleBuild> build = freeStyleProject.scheduleBuild2(0, new Cause.UserCause());
            build.get(); //// let mock build finish
        }
        TopLevelItem whateverTopLevelItem =jenkinsRule.getInstance().getItem("test-job");
        if (whateverTopLevelItem instanceof FreeStyleProject) {
            FreeStyleProject whateverFreeStyleProject = (FreeStyleProject) whateverTopLevelItem;
            Map<TriggerDescriptor, Trigger<?>> triggers = whateverFreeStyleProject.getTriggers();
            assertEquals(triggers.size(), 1);
            for (Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet()) {
                if(entry.getValue() instanceof BitBucketTrigger) {
                    BitBucketTrigger bitBucketTrigger = (BitBucketTrigger) entry.getValue();
                    List<BitbucketTriggerFilter> bitbucketTriggerFilters = bitBucketTrigger.getTriggers();
                    assertEquals(bitbucketTriggerFilters.size(), 1);
                    for (BitbucketTriggerFilter bitbucketTriggerFilter : bitbucketTriggerFilters) {
                        assertEquals(bitbucketTriggerFilter instanceof PullRequestTriggerFilter, true);
                        if (bitbucketTriggerFilter instanceof PullRequestTriggerFilter) {
                            PullRequestTriggerFilter pullRequestTriggerFilter = (PullRequestTriggerFilter) bitbucketTriggerFilter;
                            assertEquals(pullRequestTriggerFilter.getActionFilter() instanceof PullRequestCreatedActionFilter, true);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBitbucketPullRequestUpdatedAction() throws Exception {
        TopLevelItem topLevelItem = jenkinsRule.getInstance().createProjectFromXML("whatever",
                new ByteArrayInputStream((getJobDSLTrigger("freeStyleJob('test-job') { triggers{ bitbucketPullRequestUpdatedAction(false) } }\n")).getBytes()));
        if (topLevelItem instanceof FreeStyleProject) {
            FreeStyleProject freeStyleProject = (FreeStyleProject) topLevelItem;
            Future<FreeStyleBuild> build = freeStyleProject.scheduleBuild2(0, new Cause.UserCause());
            build.get(); //// let mock build finish
        }
        TopLevelItem whateverTopLevelItem =jenkinsRule.getInstance().getItem("test-job");
        if (whateverTopLevelItem instanceof FreeStyleProject) {
            FreeStyleProject whateverFreeStyleProject = (FreeStyleProject) whateverTopLevelItem;
            Map<TriggerDescriptor, Trigger<?>> triggers = whateverFreeStyleProject.getTriggers();
            assertEquals(triggers.size(), 1);
            for (Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet()) {
                if(entry.getValue() instanceof BitBucketTrigger) {
                    BitBucketTrigger bitBucketTrigger = (BitBucketTrigger) entry.getValue();
                    List<BitbucketTriggerFilter> bitbucketTriggerFilters = bitBucketTrigger.getTriggers();
                    assertEquals(bitbucketTriggerFilters.size(), 1);
                    for (BitbucketTriggerFilter bitbucketTriggerFilter : bitbucketTriggerFilters) {
                        assertEquals(bitbucketTriggerFilter instanceof PullRequestTriggerFilter, true);
                        if (bitbucketTriggerFilter instanceof PullRequestTriggerFilter) {
                            PullRequestTriggerFilter pullRequestTriggerFilter = (PullRequestTriggerFilter) bitbucketTriggerFilter;
                            assertEquals(pullRequestTriggerFilter.getActionFilter() instanceof PullRequestUpdatedActionFilter, true);
                        }
                    }
                }
            }
        }
    }

}
