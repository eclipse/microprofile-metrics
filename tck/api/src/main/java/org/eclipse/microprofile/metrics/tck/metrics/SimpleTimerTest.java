/*
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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.SimpleTimer.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class SimpleTimerTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    private static SimpleTimer globalTimer = null;

    private static boolean isInitialized = false;

    @Before
    public void initData() {
        if (isInitialized) {
            return;
        }

        globalTimer = registry.simpleTimer("test.longData.simpleTimer");

        isInitialized = true;
    }

    @Test
    @InSequence(1)
    public void testTime() throws Exception {
        SimpleTimer simpleTimer = registry.simpleTimer("testSimpleTime");

        double beforeStartTime = System.nanoTime();
        Context context = simpleTimer.time();
        double afterStartTime = System.nanoTime();
        Thread.sleep(1000);

        double beforeStopTime = System.nanoTime();
        double time = context.stop();
        double afterStopTime = System.nanoTime();

        double delta = (afterStartTime - beforeStartTime) + (afterStopTime - beforeStopTime);
        Assert.assertEquals(beforeStopTime - beforeStartTime, time, delta);
    }

    @Test
    @InSequence(2)
    public void testTimerRegistry() throws Exception {
        String simpleTimerLongName = "test.longData.simpleTimer";
        String simpleTimerTimeName = "testSimpleTime";

        MetricID simpleTimerLongNameMetricID = new MetricID(simpleTimerLongName);
        MetricID simpleTimerTimeNameMetricID = new MetricID(simpleTimerTimeName);

        Assert.assertNotNull(registry.getSimpleTimer(simpleTimerLongNameMetricID));
        Assert.assertNotNull(registry.getSimpleTimer(simpleTimerTimeNameMetricID));
    }

    @Test
    @InSequence(3)
    public void timesCallableInstances() throws Exception {
        SimpleTimer simpleTimer = registry.simpleTimer("testCallable");
        final String value = simpleTimer.time(() -> "one");

        Assert.assertEquals(simpleTimer.getCount(), 1);

        Assert.assertEquals(value, "one");
    }

    @Test
    @InSequence(4)
    public void timesRunnableInstances() throws Exception {
        SimpleTimer simpleTimer = registry.simpleTimer("testRunnable");
        final AtomicBoolean called = new AtomicBoolean();
        simpleTimer.time(() -> called.set(true));

        Assert.assertEquals(simpleTimer.getCount(), 1);

        Assert.assertEquals(called.get(), true);
    }
}
