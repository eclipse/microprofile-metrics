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
package io.astefanutti.metrics.cdi.se;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
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

@RunWith(Arquillian.class)
public class TimedMethodBeanTest {

    private final static String TIMER_NAME = MetricRegistry.name(TimedMethodBean2.class, "timedMethod");

    private static MetricID timerMID;
    
    private final static AtomicLong TIMER_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(TimedMethodBean2.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimedMethodBean2 bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API.
         * Running a managed arquillian container will result
         * with the MetricID being created in a client process
         * that does not contain the MPConfig impl.
         * 
         * This will cause client instantiated MetricIDs to 
         * throw an exception. (i.e the global MetricIDs)
         */
        timerMID = new MetricID(TIMER_NAME);
    }
    
    @Test
    @InSequence(1)
    public void timedMethodNotCalledYet() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerMID));
        Timer timer = registry.getTimers().get(timerMID);

        // Make sure that the timer hasn't been called yet
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.get())));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerMID));
        Timer timer = registry.getTimers().get(timerMID);

        // Call the timed method and assert it's been timed
        bean.timedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.incrementAndGet())));
    }

    @Test
    @InSequence(3)
    public void callSelfInvocationTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerMID));
        Timer timer = registry.getTimers().get(timerMID);

        // Call the timed method indirectly
        bean.selfInvocationTimedMethod();

        // It appears that neither Weld nor OWB support interception of self-invocation bean,
        // which is not that surprising given the use of proxies.
        // Neither the CDI nor Java Interceptors specifications make that point explicit though
        // document like http://docs.jboss.org/webbeans/spec/PDR/html/interceptors.html is stating
        // that self-invocations are considered to be business method invocations.
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.get())));
    }

    @Test
    @InSequence(4)
    public void removeTimerFromRegistry() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerMID));
        Timer timer = registry.getTimers().get(timerMID);

        // Remove the timer from metrics registry
        registry.remove(timerMID);

        try {
            // Call the timed method and assert an exception is thrown
            bean.timedMethod();
        }
        catch (RuntimeException cause) {
            assertThat(cause, is(instanceOf(IllegalStateException.class)));
            // Make sure that the timer hasn't been called
            assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.get())));
            return;
        }

        fail("No exception has been re-thrown!");
    }
}