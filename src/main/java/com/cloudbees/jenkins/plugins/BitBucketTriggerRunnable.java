package com.cloudbees.jenkins.plugins;

import hudson.Util;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.util.StreamTaskListener;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitBucketTriggerRunnable implements Runnable {
    private final String payload;
    private final Job<?, ?> job;
    private final Logger logger;
    private final String pushBy;
    private final String branchName;
    private final Boolean buildOnCreatedBranch;

    public BitBucketTriggerRunnable(String payload, Job<?, ?> job, Logger logger, String pushBy, String branchName, Boolean buildOnCreatedBranch) {
        this.payload = payload;
        this.job = job;
        this.logger = logger;
        this.pushBy = pushBy;
        this.branchName = branchName;
        this.buildOnCreatedBranch = buildOnCreatedBranch;
    }

    private boolean runPolling() {
        try {
            StreamTaskListener listener = new StreamTaskListener(getLogFile());
            try {
                PrintStream logger = listener.getLogger();
                long start = System.currentTimeMillis();
                logger.println("Started on " + DateFormat.getDateTimeInstance().format(new Date()));
                SCMTriggerItem scmTriggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
                if (scmTriggerItem == null) {
                    return false;
                } else {
                    boolean result = scmTriggerItem.poll(listener).hasChanges();
                    logger.println("Done. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));

                    if ( this.branchName == null || this.branchName.isEmpty()){
                        if (result) {
                            logger.println("Changes found");
                        } else {
                            logger.println("No changes");
                        }
                    } else {
                        if ( this.buildOnCreatedBranch){
                            logger.println("Branch [" + this.branchName + "] was created");
                            return true;
                        } else {
                            logger.println("Branch [" + this.branchName + "] was created but \"Build on branch created\" is false, not triggering");
                            return false;
                        }


                    }

                    return result;
                }
            } catch (Error | RuntimeException e) {
                e.printStackTrace(listener.error("Failed to record SCM polling"));
                logger.log(Level.SEVERE, "Failed to record SCM polling", e);
                throw e;
            } finally {
                listener.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to record SCM polling", e);
        }
        return false;
    }

    public void run() {
        if (job == null) {
            logger.info("job is null");
            return;
        }


        if (runPolling()) {
            buildJob();
        }


    }

    private void buildJob() {
        BitBucketPushCause cause;
        String name = " #" + job.getNextBuildNumber();
        try {
            cause = new BitBucketPushCause(getLogFile(), pushBy);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse the polling log", e);
            cause = new BitBucketPushCause(pushBy);
        }
        ParameterizedJobMixIn pJob = new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return job;
            }
        };
        BitBucketPayload bitBucketPayload = new BitBucketPayload(payload);
        pJob.scheduleBuild2(5, new CauseAction(cause), bitBucketPayload);
        if (pJob.scheduleBuild(cause)) {
            if ( this.branchName == null || this.branchName.isEmpty()){
                logger.info("SCM changes detected in [" + job.getName() + "]. Triggering [" + name + "]");
            } else {
                if ( this.buildOnCreatedBranch){
                    logger.info("Branch [" + this.branchName + "] created. Triggering [" + name + "]");
                } else {
                    logger.info("Branch [" + this.branchName + "] created. but [Build on branch created] is false, not building");
                }
            }
        } else {
            logger.info("SCM changes detected in " + job.getName() + ". Job is already in the queue");
        }
    }

    public File getLogFile() {
        if (job == null) {
            throw new RuntimeException("job is null");
        } else {
            return new File(job.getRootDir(), "bitbucket-polling.log");
        }

    }
}
