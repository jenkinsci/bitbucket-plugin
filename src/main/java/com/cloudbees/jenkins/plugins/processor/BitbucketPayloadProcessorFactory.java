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

package com.cloudbees.jenkins.plugins.processor;

import com.cloudbees.jenkins.plugins.BitbucketEvent;
import com.cloudbees.jenkins.plugins.BitbucketJobProbe;

/**
 * The factory for the {@link BitbucketPayloadProcessor}
 * @since August 1, 2016
 * @version 2.0
 */
public class BitbucketPayloadProcessorFactory {
    /**
     * Creates a {@link BitbucketPayloadProcessor} based on the {@link BitbucketEvent}
     *
     * @return {@link BitbucketPayloadProcessor}
     */
    public BitbucketPayloadProcessor create(BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName())) {
            return new RepositoryPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        } else if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName())) {
            return new PullRequestPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
        }

        throw new UnsupportedOperationException("Bitbucket event " + bitbucketEvent.getName() + " not supported");
    }

    public BitbucketPayloadProcessor create(BitbucketJobProbe probe, BitbucketEvent bitbucketEvent) {
        if(BitbucketEvent.EVENT.REPOSITORY.equals(bitbucketEvent.getName())) {
            return new RepositoryPayloadProcessor(probe, bitbucketEvent);
        } else if(BitbucketEvent.EVENT.PULL_REQUEST.equals(bitbucketEvent.getName())) {
            return new PullRequestPayloadProcessor(probe, bitbucketEvent);
        }

        return null;
    }

    /**
     * Creates a {@link BitbucketPayloadProcessor} based on the {@link BitbucketEvent}
     *
     * @return {@link BitbucketPayloadProcessor}
     */
    public BitbucketPayloadProcessor createOldProcessor(BitbucketEvent bitbucketEvent) {
        return new OldPostPayloadProcessor(new BitbucketJobProbe(), bitbucketEvent);
    }

}