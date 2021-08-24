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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.tck.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class SimplyTimedMethodBeanTest {

    private final static String SIMPLE_TIMER_NAME =
            MetricRegistry.name(SimplyTimedMethodBean2.class, "simplyTimedMethod");

    private static MetricID simpleTimerMID;

    private final static AtomicLong SIMPLE_TIMER_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(SimplyTimedMethodBean2.class)
                .addClass(TestUtils.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private SimplyTimedMethodBean2 bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        simpleTimerMID = new MetricID(SIMPLE_TIMER_NAME);
    }

    @Test
    @InSequence(1)
    public void simplyTimedMethodNotCalledYet() {
        SimpleTimer simpleTimer = registry.getSimpleTimer(simpleTimerMID);
        assertThat("SimpleTimer is not registered correctly", simpleTimer, notNullValue());

        // Make sure that the simpleTimer hasn't been called yet
        assertThat("SimpleTimer count is incorrect", simpleTimer.getCount(), is(equalTo(SIMPLE_TIMER_COUNT.get())));

    }

    @Test
    @InSequence(2)
    public void callSimplyTimedMethodOnce() throws InterruptedException {
        SimpleTimer simpleTimer = registry.getSimpleTimer(simpleTimerMID);
        assertThat("SimpleTimer is not registered correctly", simpleTimer, notNullValue());

        // Call the simplyTimed method and assert it's been simplyTimed
        bean.simplyTimedMethod();

        // Make sure that the simpleTimer has been called
        assertThat("SimpleTimer count is incorrect", simpleTimer.getCount(),
                is(equalTo(SIMPLE_TIMER_COUNT.incrementAndGet())));
        TestUtils.assertEqualsWithTolerance(2000000000L, simpleTimer.getElapsedTime().toNanos());
    }

    @Test
    @InSequence(3)
    public void removeSimpleTimerFromRegistry() throws InterruptedException {
        SimpleTimer simpleTimer = registry.getSimpleTimer(simpleTimerMID);
        assertThat("SimpleTimer is not registered correctly", simpleTimer, notNullValue());

        // Remove the simpleTimer from metrics registry
        registry.remove(simpleTimerMID);

        try {
            // Call the simplyTimed method and assert an exception is thrown
            bean.simplyTimedMethod();
        } catch (RuntimeException cause) {
            assertThat(cause, is(instanceOf(IllegalStateException.class)));
            // Make sure that the simpleTimer hasn't been called
            assertThat("SimpleTimer count is incorrect", simpleTimer.getCount(), is(equalTo(SIMPLE_TIMER_COUNT.get())));
            return;
        }

        fail("No exception has been re-thrown!");
    }
}
