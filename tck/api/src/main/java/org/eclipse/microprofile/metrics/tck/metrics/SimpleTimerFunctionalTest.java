/**
 * ********************************************************************
 *  Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeoutException;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.tck.util.TimeUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class SimpleTimerFunctionalTest {

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(SimpleTimerFunctionalTest.class)
                .addClass(SimpleTimerFunctionalBean.class)
                .addClass(TimeUtil.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private SimpleTimerFunctionalBean bean;

    /**
     * This test will test that the min and max values are the same if only one invocation of the bean's method has
     * occured
     */
    @Test
    @InSequence(1)
    public void testMinMaxEqual() throws TimeoutException, InterruptedException {
        TimeUtil.waitForNextMinute();
        bean.doSomething();

        // The min and max should be null right now
        assertNull("Minimum should be null",
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMinTimeDuration());
        assertNull("Maximum should be null",
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMaxTimeDuration());

        TimeUtil.waitForNextMinute();

        // The min and max values should NOT be null right now
        assertNotNull("Minimum should NOT be null",
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMinTimeDuration());
        assertNotNull("Maximum should NOT be null",
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMaxTimeDuration());

        // The min and max values should be the SAME
        assertEquals("Minimum and Maximum should contain the same value ",
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMaxTimeDuration(),
                metricRegistry.getSimpleTimers().get(new MetricID("mySimplyTimed")).getMinTimeDuration());

    }

}
