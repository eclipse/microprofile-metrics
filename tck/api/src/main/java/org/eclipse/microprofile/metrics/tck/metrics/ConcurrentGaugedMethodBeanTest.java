/*
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Metric;
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
public class ConcurrentGaugedMethodBeanTest {

    private final static String C_GAUGE_NAME = "cGaugedMethod";
    private static MetricID cGaugeMID;

    private final static AtomicLong COUNTER_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(ConcurrentGaugedMethodBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private ConcurrentGaugedMethodBean<Long> bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        cGaugeMID = new MetricID(C_GAUGE_NAME);
    }

    @Test
    @InSequence(1)
    public void countedMethodNotCalledYet() {
        ConcurrentGauge cGauge = registry.getConcurrentGauge(cGaugeMID);
        assertThat("Concurrent Gauges is not registered correctly", cGauge, notNullValue());

        // Make sure that the counter hasn't been called yet
        assertThat("Concurrent Gauges count is incorrect", cGauge.getCount(), is(equalTo(COUNTER_COUNT.get())));
    }

    @Test
    @InSequence(2)
    public void metricInjectionIntoTest(@Metric(name = C_GAUGE_NAME, absolute = true) ConcurrentGauge instance) {
        ConcurrentGauge cGauge = registry.getConcurrentGauge(cGaugeMID);
        assertThat("Concurrent Gauges is not registered correctly", cGauge, notNullValue());

        // Make sure that the counter registered and the bean instance are the same
        assertThat("Concurrent Gauges and bean instance are not equal", instance, is(equalTo(cGauge)));
    }

    @Test
    @InSequence(3)
    public void callCountedMethodOnce() throws InterruptedException, TimeoutException {
        ConcurrentGauge cGauge = registry.getConcurrentGauge(cGaugeMID);
        assertThat("Concurrent Gauges is not registered correctly", cGauge, notNullValue());

        // Call the counted method, block and assert it's been counted
        final Exchanger<Long> exchanger = new Exchanger<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exchanger.exchange(bean.countedMethod(new Callable<Long>() {
                        @Override
                        public Long call() throws Exception {
                            exchanger.exchange(0L);
                            return exchanger.exchange(0L);
                        }
                    }));
                } catch (InterruptedException cause) {
                    throw new RuntimeException(cause);
                }
            }
        });
        final AtomicInteger uncaught = new AtomicInteger();
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                uncaught.incrementAndGet();
            }
        });
        thread.start();

        // Wait until the method is executing and make sure that the counter has been incremented
        exchanger.exchange(0L, 5L, TimeUnit.SECONDS);
        assertThat("Concurrent Gauges count is incorrect", cGauge.getCount(),
                is(equalTo(COUNTER_COUNT.incrementAndGet())));

        // Exchange the result and unblock the method execution
        Long random = 1 + Math.round(Math.random() * (Long.MAX_VALUE - 1));
        exchanger.exchange(random, 5L, TimeUnit.SECONDS);

        // Wait until the method has returned
        assertThat("Concurrent Gauges method return value is incorrect", exchanger.exchange(0L), is(equalTo(random)));

        // Then make sure that the counter has been decremented
        assertThat("Concurrent Gauges count is incorrect", cGauge.getCount(),
                is(equalTo(COUNTER_COUNT.decrementAndGet())));

        // Finally make sure calling thread is returns correctly
        thread.join();
        assertThat("Exception thrown in method call thread", uncaught.get(), is(equalTo(0)));
    }

    @Test
    @InSequence(4)
    public void removeCounterFromRegistry() {
        ConcurrentGauge cGauge = registry.getConcurrentGauge(cGaugeMID);
        assertThat("Concurrent Gauge is not registered correctly", cGauge, notNullValue());

        // Remove the counter from metrics registry
        registry.remove(cGaugeMID);

        try {
            // Call the counted method and assert an exception is thrown
            bean.countedMethod(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return null;
                }
            });
        } catch (Exception cause) {
            assertThat(cause, is(instanceOf(IllegalStateException.class)));
            // Make sure that the counter hasn't been called
            assertThat("Concurrent Gauges count is incorrect", cGauge.getCount(), is(equalTo(COUNTER_COUNT.get())));
            return;
        }

        fail("No exception has been re-thrown!");
    }
}
