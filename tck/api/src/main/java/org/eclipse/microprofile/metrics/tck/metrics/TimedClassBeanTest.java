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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.tck.util.MetricsUtil;
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
public class TimedClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "TimedClassBean";

    private static final String CONSTRUCTOR_TIMER_NAME =
            MetricsUtil.absoluteMetricName(TimedClassBean.class, "timedClass", CONSTRUCTOR_NAME);

    private static MetricID constructorMID;

    private static final String[] METHOD_NAMES =
            {"timedMethodOne", "timedMethodTwo", "timedMethodProtected", "timedMethodPackagedPrivate"};

    private static final Set<String> METHOD_TIMER_NAMES =
            MetricsUtil.absoluteMetricNames(TimedClassBean.class, "timedClass", METHOD_NAMES);

    private static final MetricFilter METHOD_TIMERS = new MetricFilter() {
        @Override
        public boolean matches(MetricID metricID, Metric metric) {
            return METHOD_TIMER_NAMES.contains(metricID.getName());
        }
    };

    private static final Set<String> TIMER_NAMES =
            MetricsUtil.absoluteMetricNames(TimedClassBean.class, "timedClass", METHOD_NAMES,
                    CONSTRUCTOR_NAME);

    private static Set<MetricID> timerMIDs;

    // toString is overridden just to be able to trigger instantiation of the bean by calling it
    // it's otherwise irrelevant to the test, but it will also get a metric for itself
    private static Set<MetricID> timerMIDsIncludingToString;

    private static final AtomicLong METHOD_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(TimedClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimedClassBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean
        // explicitly
        // as only a proxy gets injected otherwise
        bean.toString();
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        constructorMID = new MetricID(CONSTRUCTOR_TIMER_NAME);
        timerMIDs = MetricsUtil.createMetricIDs(TIMER_NAMES);

        timerMIDsIncludingToString = new HashSet<>();
        timerMIDsIncludingToString.addAll(timerMIDs);
        timerMIDsIncludingToString.addAll(MetricsUtil.createMetricIDs(
                MetricsUtil.absoluteMetricNames(TimedClassBean.class, "timedClass", new String[]{"toString"})));
    }

    @Test
    @InSequence(1)
    public void timedMethodsNotCalledYet() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(),
                is(equalTo(timerMIDsIncludingToString)));

        assertThat("Constructor timer count is incorrect", registry.getTimer(constructorMID).getCount(),
                is(equalTo(1L)));

        // Make sure that the method timers haven't been timed yet
        assertThat("Method timer counts are incorrect", registry.getTimers(METHOD_TIMERS).values(),
                everyItem(Matchers.<Timer>hasProperty("count", equalTo(METHOD_COUNT.get()))));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodsOnce() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(),
                is(equalTo(timerMIDsIncludingToString)));

        assertThat("Constructor timer count is incorrect", registry.getTimer(constructorMID).getCount(),
                is(equalTo(1L)));

        // Call the timed methods and assert they've been timed
        bean.timedMethodOne();
        bean.timedMethodTwo();
        // Let's call the non-public methods as well
        bean.timedMethodProtected();
        bean.timedMethodPackagedPrivate();

        // Make sure that the method timers have been timed
        assertThat("Method timer counts are incorrect", registry.getTimers(METHOD_TIMERS).values(),
                everyItem(Matchers.<Timer>hasProperty("count", equalTo(METHOD_COUNT.incrementAndGet()))));
    }
}
