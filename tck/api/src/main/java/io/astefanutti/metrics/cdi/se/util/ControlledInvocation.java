/**
 * ********************************************************************
 *  Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 *  See the NOTICES file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ********************************************************************
 *
 */
package io.astefanutti.metrics.cdi.se.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ControlledInvocation {

    private final BeanWithControlledInvocation bean;

    // used to mark when the invocation has already started so the concurrent gauge was incremented
    // the bean itself should call countDown
    private final CountDownLatch startMarker;

    // used to instruct the invocation to finish
    // the invocation task calls countDown
    private final CountDownLatch endMarker;

    // used to mark when the invocation has already finished so the concurrent gauge was decremented
    private final CountDownLatch endCommand;

    public ControlledInvocation(BeanWithControlledInvocation bean) {
        this.bean = bean;
        this.startMarker = new CountDownLatch(1);
        this.endCommand = new CountDownLatch(1);
        this.endMarker = new CountDownLatch(1);
    }

    /**
     * Start the invocation and synchronously wait until we are sure that the relevant metrics were updated.
     */
    public void start() throws InterruptedException, TimeoutException {
        new Thread(
            () -> {
                bean.controlledMethod(startMarker, endCommand);
                endMarker.countDown();
            }
        ).start();
        if(!startMarker.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException();
        }
    }

    /**
     * Stop the invocation and synchronously wait until we are sure that the relevant metrics were updated.
     */
    public void stop() {
        try {
            // do nothing if stop was already called before
            // do nothing if start was never called
            if (endCommand.getCount() != 0 && startMarker.getCount() == 0) {
                endCommand.countDown();
                if (!endMarker.await(5, TimeUnit.SECONDS)) {
                    throw new TimeoutException();
                }
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
