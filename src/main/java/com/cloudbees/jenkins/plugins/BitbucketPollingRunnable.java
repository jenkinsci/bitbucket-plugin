package com.cloudbees.jenkins.plugins;

import hudson.Util;
import hudson.model.Job;
import hudson.scm.PollingResult;
import hudson.util.StreamTaskListener;
import jenkins.triggers.SCMTriggerItem;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shyri Villar on 11/03/2016.
 */
public class BitbucketPollingRunnable implements Runnable{
    Job job;
    File logFile;

    BitbucketPollResultListener bitbucketPollResultListener;

    public BitbucketPollingRunnable(Job job, File logFile, BitbucketPollResultListener bitbucketPollResultListener) {
        this.job = job;
        this.bitbucketPollResultListener = bitbucketPollResultListener;
        this.logFile = logFile;
    }

    @Override
    public void run() {
        try {
            StreamTaskListener streamListener = new StreamTaskListener(logFile);

            try {
                PrintStream logger = streamListener.getLogger();

                long start = System.currentTimeMillis();
                logger.println("Started on " + DateFormat.getDateTimeInstance().format(new Date()));

                PollingResult pollingResult = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job).poll(streamListener);

                logger.println("Done. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));

                bitbucketPollResultListener.onPollSuccess(pollingResult);
            } catch (Error e) {
                e.printStackTrace(streamListener.error("Failed to record SCM polling"));
                LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
                bitbucketPollResultListener.onPollError(e);
            } catch (RuntimeException e) {
                e.printStackTrace(streamListener.error("Failed to record SCM polling"));
                LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
                bitbucketPollResultListener.onPollError(e);
            } finally {
                streamListener.close();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
            bitbucketPollResultListener.onPollError(e);
        }
    }

    public interface BitbucketPollResultListener {
        void onPollSuccess(PollingResult pollingResult);
        void onPollError(Throwable throwable);
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPollingRunnable.class.getName());
}
