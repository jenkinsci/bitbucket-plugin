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

package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryPushActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BitBucketTriggerTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @LocalData
    @Test
    public void testReadResolve() throws Exception {
        Item bitbucketEnabledTriggerItem =  jenkinsRule.getInstance().getItemByFullName("job-with-bitbucket-trigger-enabled");
        Item bitbucketDisabledTriggerItem =  jenkinsRule.getInstance().getItemByFullName("job-with-bitbucket-trigger-disabled");
        Map<TriggerDescriptor, Trigger<?>> triggers = ((FreeStyleProject) bitbucketEnabledTriggerItem).getTriggers();
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
        Map<TriggerDescriptor, Trigger<?>> triggerDisabledTriggers = ((FreeStyleProject) bitbucketDisabledTriggerItem).getTriggers();
        assertEquals(triggerDisabledTriggers.size(), 0);
    }

}
