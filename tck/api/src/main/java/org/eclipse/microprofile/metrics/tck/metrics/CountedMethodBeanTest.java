/**
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
package org.eclipse.microprofile.metrics.tck.metrics;

import static org.hamcrest.Matchers.equalTo;
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

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.hamcrest.Matchers;
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
public class CountedMethodBeanTest {

    private final static String COUNTER_NAME = "countedMethod";
    private static MetricID counterMetricID; // new MetricID(COUNTER_NAME);

    private final static AtomicLong COUNTER_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(CountedMethodBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private CountedMethodBean<Long> bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        counterMetricID = new MetricID(COUNTER_NAME);
    }

    @Test
    @InSequence(1)
    public void countedMethodNotCalledYet() {
        Counter counter = registry.getCounter(counterMetricID);
        assertThat("Counter is not registered correctly", counter, notNullValue());

        // Make sure that the counter hasn't been called yet
        assertThat("Counter count is incorrect", counter.getCount(), is(equalTo(COUNTER_COUNT.get())));
    }

    @Test
    @InSequence(2)
    public void metricInjectionIntoTest(@Metric(name = "countedMethod", absolute = true) Counter instance) {
        Counter counter = registry.getCounter(counterMetricID);
        assertThat("Counter is not registered correctly", counter, notNullValue());

        // Make sure that the counter registered and the bean instance are the same
        assertThat("Counter and bean instance are not equal", instance, is(equalTo(counter)));
    }

    @Test
    @InSequence(3)
    public void callCountedMethodOnce() throws InterruptedException, TimeoutException {
        Counter counter = registry.getCounter(counterMetricID);
        assertThat("Counter is not registered correctly", counter, notNullValue());

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
                e.printStackTrace();
                uncaught.incrementAndGet();
            }
        });
        thread.start();

        // Wait until the method is executing and make sure that the counter has been incremented
        exchanger.exchange(0L, 5L, TimeUnit.SECONDS);
        assertThat("Counter count is incorrect", counter.getCount(), is(equalTo(COUNTER_COUNT.incrementAndGet())));

        // Exchange the result and unblock the method execution
        Long random = 1 + Math.round(Math.random() * (Long.MAX_VALUE - 1));
        exchanger.exchange(random, 5L, TimeUnit.SECONDS);

        // Wait until the method has returned
        assertThat("Counted method return value is incorrect", exchanger.exchange(0L), is(equalTo(random)));

        // Then make sure that the counter has not been decremented
        assertThat("Counter count is incorrect", counter.getCount(), is(equalTo(COUNTER_COUNT.get())));

        // Finally make sure calling thread is returns correctly
        thread.join();
        assertThat("Exception thrown in method call thread", uncaught.get(), is(equalTo(0)));
    }

    @Test
    @InSequence(4)
    public void removeCounterFromRegistry() {
        Counter counter = registry.getCounter(counterMetricID);
        assertThat("Counter is not registered correctly", counter, notNullValue());

        // Remove the counter from metrics registry
        registry.remove(counterMetricID);

        try {
            // Call the counted method and assert an exception is thrown
            bean.countedMethod(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return null;
                }
            });
        } catch (Exception cause) {
            assertThat(cause, is(Matchers.<Exception>instanceOf(IllegalStateException.class)));
            // Make sure that the counter hasn't been called
            assertThat("Counter count is incorrect", counter.getCount(), is(equalTo(COUNTER_COUNT.get())));
            return;
        }

        fail("No exception has been re-thrown!");
    }
}
