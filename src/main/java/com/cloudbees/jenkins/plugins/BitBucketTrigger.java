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

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.BitbucketTriggerFilterDescriptor;
import com.cloudbees.jenkins.plugins.filter.FilterMatcher;
import com.cloudbees.jenkins.plugins.filter.pullrequest.PullRequestTriggerFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryPushActionFilter;
import com.cloudbees.jenkins.plugins.filter.repository.RepositoryTriggerFilter;
import com.cloudbees.jenkins.plugins.payload.BitbucketPayload;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import hudson.scm.PollingResult;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Processes the HTTP POST requests received by {@link BitbucketHookReceiver}
 * @version 1.0
 */
public class BitBucketTrigger extends Trigger<Job<?, ?>> {
    public List<BitbucketTriggerFilter> triggers;

    @DataBoundConstructor
    public BitBucketTrigger(List<BitbucketTriggerFilter> triggers) {
        this.triggers = triggers;
    }
    
    @Override
    public Object readResolve() throws ObjectStreamException {
        super.readResolve();
        if (triggers == null) {
            RepositoryPushActionFilter repositoryPushActionFilter = new RepositoryPushActionFilter();
            RepositoryTriggerFilter repositoryTriggerFilter = new RepositoryTriggerFilter(repositoryPushActionFilter);
            triggers = new ArrayList<BitbucketTriggerFilter>();
            triggers.add(repositoryTriggerFilter);
        }
        return this;
    }
    /**
     * Called when a POST is made.
     */
    public void onPost(final BitbucketEvent bitbucketEvent, final BitbucketPayload bitbucketPayload) {
        FilterMatcher filterMatcher = new FilterMatcher();

        final List<BitbucketTriggerFilter> matchingFilters = filterMatcher.getMatchingFilters(bitbucketEvent, triggers);

        if(matchingFilters != null) {
            if(matchingFilters.size() > 0) {
                BitbucketPollingRunnable bitbucketPollingRunnable = new BitbucketPollingRunnable(job,
                    getLogFile(),
                    new BitbucketPollingRunnable.BitbucketPollResultListener() {
                        @Override
                        public void onPollSuccess(PollingResult pollingResult) {
                            LOGGER.log(Level.FINEST, "Called onPollSuccess");
                            for (BitbucketTriggerFilter filter : matchingFilters) {
                                BitbucketTriggerCause cause;
                                try {
                                    cause = filter.getCause(getLogFile(), bitbucketPayload);


                                    if (shouldScheduleJob(filter, pollingResult, bitbucketPayload)) {
                                        scheduleJob(cause, bitbucketPayload);
                                        return;
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onPollError(Throwable throwable) {
                            LOGGER.log(Level.FINEST, "Called onPollError");
                        }
                    });
                getDescriptor().queue.execute(bitbucketPollingRunnable);
            }else{
                LOGGER.log(Level.FINEST, "Size is < zero");
            }
        }else{
            LOGGER.log(Level.FINEST, "No matching filters");
        }
    }

    private boolean shouldScheduleJob(BitbucketTriggerFilter filter, PollingResult pollingResult, BitbucketPayload bitbucketPayload){
        boolean shouldScheduleJob = filter.shouldScheduleJob(bitbucketPayload);
        boolean hasChanges = pollingResult.hasChanges();
        boolean isPullRequestFilter = filter instanceof PullRequestTriggerFilter;
        LOGGER.log(Level.FINEST, "Should schedule job : {0} and polling result has changes {1} and is instance of {2}", new Object[]{shouldScheduleJob, hasChanges, isPullRequestFilter});
        return shouldScheduleJob && (hasChanges || isPullRequestFilter);
    }

    private void scheduleJob(BitbucketTriggerCause cause, BitbucketPayload bitbucketPayload) {
        ParameterizedJobMixIn pJob = new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return job;
            }
        };

        pJob.scheduleBuild2(5, new CauseAction(cause), bitbucketPayload);
        if (pJob.scheduleBuild(cause)) {
            String name = " #" + job.getNextBuildNumber();
            LOGGER.info("SCM changes detected in " + job.getName() + ". Triggering " + name);
        } else {
            LOGGER.info("SCM changes detected in " + job.getName() + ". Job is already in the queue");
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new BitBucketWebHookPollingAction());
    }

    /**
     * Returns the file that records the last/current polling activity.
     */
    public File getLogFile() {
        return new File(job.getRootDir(), "bitbucket-polling.log");
    }

    /**
     * Check if "bitbucket-polling.log" already exists to initialize it
     */
    public boolean IsLogFileInitialized() {
        File file = new File(job.getRootDir(), "bitbucket-polling.log");
        return file.exists();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Action object for {@link Project}. Used to display the polling log.
     */
    public final class BitBucketWebHookPollingAction implements Action {
        public Job<?, ?> getOwner() {
            return job;
        }

        public String getIconFileName() {
            return "clipboard.png";
        }

        public String getDisplayName() {
            return "BitBucket Hook Log";
        }

        public String getUrlName() {
            return "BitBucketPollLog";
        }

        public String getLog() throws IOException {
            return Util.loadFile(getLogFile());
        }

        /**
         * Writes the annotated log to the given output.
         */
        public void writeLogTo(XMLOutput out) throws IOException {
            new AnnotatedLargeText<BitBucketWebHookPollingAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
        }
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {
        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Hudson.MasterComputer.threadPoolForRemoting);

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof Job && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to BitBucket";
        }


        public List<BitbucketTriggerFilterDescriptor> getTriggerDescriptors() {
            // you may want to filter this list of descriptors here, if you are being very fancy
            return Jenkins.getInstance().getDescriptorList(BitbucketTriggerFilter.class);
        }

    }

    public List<BitbucketTriggerFilter> getTriggers() {
        return triggers;
    }

    private static final Logger LOGGER = Logger.getLogger(BitBucketTrigger.class.getName());
}
