package com.cloudbees.jenkins.plugins;

import hudson.triggers.SCMTrigger;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BitBucketPushCause extends SCMTrigger.SCMTriggerCause {

    private final String pushedBy;

    public BitBucketPushCause(String pusher) {
        this("", pusher);
    }
    
    public BitBucketPushCause(String pollingLog, String pusher) {
        super(pollingLog);
        pushedBy = pusher;
    }

    public BitBucketPushCause(File pollingLog, String pusher) throws IOException {
        super(pollingLog);
        pushedBy = pusher;
    }

    @Override
    public String getShortDescription() {
        String pusher = pushedBy != null ? pushedBy : "";
        return "Started by BitBucket push by " + pusher;
    }

    @Override
    public boolean equals(Object o) {
        if ( o == this){
            return true;
        } else if ( o instanceof BitBucketPushCause) {
            if (super.equals(o)) {
                BitBucketPushCause bitBucketPushCause = (BitBucketPushCause) o;
                return Objects.equals(bitBucketPushCause.pushedBy, this.pushedBy);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pushedBy);
    }
}
