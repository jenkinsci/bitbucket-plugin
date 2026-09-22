package com.cloudbees.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.EnvironmentContributingAction;
import hudson.model.Run;
import hudson.model.Queue;

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
 * @since September 22, 2026
 * @version 1.1.6
 */
public class BitBucketPayload extends CauseAction implements EnvironmentContributingAction {
    private final @NonNull String payload;

    public BitBucketPayload(Cause cause, @NonNull String payload) {
        super(cause);
        this.payload = payload;
    }

    @Override
    public void foldIntoExisting(Queue.Item item, Queue.Task owner, List<Action> otherActions) {
        super.foldIntoExisting(item, owner, otherActions);
        item.addAction(this);
    }

    @NonNull
    public String getPayload() {
        return payload;
    }

    @Override
    public void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars env) {
        EnvironmentContributingAction.super.buildEnvironment(run, env);
        final String payload = getPayload();
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOAD: {0}", payload);
        env.put("BITBUCKET_PAYLOAD", payload);

        final String currentPayloads = env.get("BITBUCKET_PAYLOADS");
        JSONArray allPayloadsJSON;
        if (currentPayloads != null && !currentPayloads.trim().isEmpty()) {
            allPayloadsJSON = (JSONArray) JSONSerializer.toJSON(currentPayloads);
        } else {
            allPayloadsJSON = new JSONArray();
        }
        if (payload != null && !payload.trim().isEmpty()) {
            allPayloadsJSON.add((JSONObject) JSONSerializer.toJSON(payload));
        }
        String allPayloads = allPayloadsJSON.toString();
        LOGGER.log(Level.FINEST, "Injecting BITBUCKET_PAYLOADS: {0}", allPayloads);
        env.put("BITBUCKET_PAYLOADS", allPayloads);
    }

    private static final Logger LOGGER = Logger.getLogger(BitBucketPayload.class.getName());
}
