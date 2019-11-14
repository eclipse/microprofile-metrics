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
package org.eclipse.microprofile.metrics.tck.metrics;

import org.eclipse.microprofile.metrics.tck.util.ControlledInvocation;
import org.eclipse.microprofile.metrics.tck.util.BeanWithControlledInvocation;
import org.eclipse.microprofile.metrics.tck.util.TimeUtil;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ConcurrentGaugeFunctionalTest {

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(ConcurrentGaugeFunctionalTest.class)
            .addClass(ConcurrentGaugeFunctionalBean.class)
            .addClass(BeanWithControlledInvocation.class)
            .addClass(ControlledInvocation.class)
            .addClass(TimeUtil.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private ConcurrentGaugeFunctionalBean bean;

    static final int NUMBER_OF_INVOCATIONS = 20;


    /**
     * To test the 'min' and 'max' values we have to do this:
     * - run invocation1 and keep it running
     * - wait until the next minute starts
     * - run invocation2 and stop it right away
     * - wait until the next minute starts
     * - stop invocation1
     * - after this, 'min' should be 1 and 'max' should be 2
     */
    @Test
    @InSequence(1)
    public void testMinMax() throws TimeoutException, InterruptedException {
        ControlledInvocation invocation1 = new ControlledInvocation(bean);
        ControlledInvocation invocation2 =  new ControlledInvocation(bean);
        invocation1.start();
        try {
            TimeUtil.waitForNextMinute();
            invocation2 = new ControlledInvocation(bean);
            invocation2.start();
            invocation2.stop();
            TimeUtil.waitForNextMinute();
            invocation1.stop();
            assertEquals("Minimum should be 1 ", 1,
                metricRegistry.getConcurrentGauges().get(new MetricID("mygauge")).getMin());
            assertEquals("Maximum should be 2", 2,
                metricRegistry.getConcurrentGauges().get(new MetricID("mygauge")).getMax());
        }
        finally {
            invocation1.stop();
            invocation2.stop();
        }
    }

    /**
     * Over time, run multiple invocations on the bean (so that at one point, all are running at the same time).
     * Over time, check that the concurrent gauge's value is updated properly.
     * After that, start stopping the invocations one by one, and again, check that the concurrent gauge is updated.
     */
    @Test
    @InSequence(2)
    public void testConcurrentInvocations() throws InterruptedException, TimeoutException {
        ControlledInvocation[] invocations = new ControlledInvocation[NUMBER_OF_INVOCATIONS];
        try {
            // run some clients for the first method, see the 'count' of the concurrent gauge increment over time
            for (int i = 0; i < NUMBER_OF_INVOCATIONS; i++) {
                invocations[i] = new ControlledInvocation(bean);
                invocations[i].start();
                assertEquals(i + 1,
                    metricRegistry.getConcurrentGauges().get(new MetricID("mygauge")).getCount());
            }
            // stop all clients and see the 'count' of the concurrent gauge decrement over time
            for (int i = 0; i < NUMBER_OF_INVOCATIONS; i++) {
                invocations[i].stop();
                assertEquals(NUMBER_OF_INVOCATIONS - i - 1,
                    metricRegistry.getConcurrentGauges().get(new MetricID("mygauge")).getCount());
            }
        }
        finally {
            try {
                for(ControlledInvocation invocation : invocations) {
                    invocation.stop();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
