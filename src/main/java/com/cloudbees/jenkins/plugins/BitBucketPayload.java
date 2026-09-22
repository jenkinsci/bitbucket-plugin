package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.Action;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.model.Queue;
import hudson.model.queue.FoldableAction;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Inject the payload received by BitBucket into the build through $BITBUCKET_PAYLOAD so it can be processed
 * Concatenate payloads of BitBucket webhooks ignored by Jenkins within quiet period into $BITBUCKET_PAYLOADS
 * The class extends FoldableAction to support payload capture when multiple webhooks
 * trigger the job at nearly same time when concurrent builds are disabled. In this case Jenkins Queue
 * schedules only one build (within quiet period) and ignores the others as "duplicates", but their 
 * FoldableAction are appended into the action list of the first scheduled build, so no one webhook gets lost.
 * @since September 18, 2026
 * @version 1.1.6
 */
public class BitBucketPayload extends InvisibleAction implements FoldableAction, EnvironmentContributingAction {
    private final @NonNull String payload;
    private List<BitBucketPayload> independentActions = new ArrayList<>();

    public BitBucketPayload(@NonNull String payload) {
        this.payload = payload;
        this.independentActions.add(this);
    }

    public BitBucketPayload (BitBucketPayload obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Source BitBucketPayload object cannot be null");
        }
        this.payload = obj.payload;
        this.independentActions = new ArrayList<>();
        if (obj.independentActions != null) {
            this.independentActions.addAll(obj.independentActions);
        }
    }

    // Synchronize to safely support cross-thread queue merging
    public synchronized void addData(List<BitBucketPayload> newData) {
        // Jenkins/XStream deserialization: If an old build.xml doesn't have this list initialized, fix it now
        if (this.independentActions == null) {
            this.independentActions = new ArrayList<>();
        }
        this.independentActions.addAll(newData);
    }

    public synchronized List<BitBucketPayload> getIndependentActions() {
        // Jenkins/XStream deserialization: If an old build.xml doesn't have this list initialized, fix it now
        if (this.independentActions == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(independentActions); // return a safe copy
    }

    @Override
    public void foldIntoExisting(Queue.Item item, Queue.Task owner, List<Action> otherActions) {
        BitBucketPayload existing = item.getAction(BitBucketPayload.class);
        if (existing != null) {
            existing.addData(this.getIndependentActions());
            return;
        }
        item.addAction(new BitBucketPayload(this.payload));
    }

    @NonNull
    public String getPayload() {
        return payload;
    }

    @Override
    public void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars env) {
        EnvironmentContributingAction.super.buildEnvironment(run, env);
        String payload = getPayload();
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payload);
        env.put("BITBUCKET_PAYLOAD", payload);

        JSONArray allPayloadsJSON = new JSONArray();
        for (BitBucketPayload data : getIndependentActions()) {
            payload = data.getPayload();
            if (payload != null && !payload.trim().isEmpty()) {
                allPayloadsJSON.add((JSONObject) JSONSerializer.toJSON(payload));
            }
        }

        String allPayloads = allPayloadsJSON.toString();
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOADS: {0}", allPayloads);
        env.put("BITBUCKET_PAYLOADS", allPayloads);
    }

    private static final Logger LOGGER = Logger.getLogger(BitBucketPayload.class.getName());
}
