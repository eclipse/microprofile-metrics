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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

@RunWith(Arquillian.class)
public class MonotonicCountedClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "MonotonicCountedClassBean";

    private static final String CONSTRUCTOR_COUNTER_NAME = MetricsUtil.absoluteMetricName(MonotonicCountedClassBean.class, "monotonicCountedClass",
            CONSTRUCTOR_NAME);

    private static final String[] METHOD_NAMES = { "countedMethodOne", "countedMethodTwo", "countedMethodProtected", "countedMethodPackagedPrivate" };

    private static final Set<String> METHOD_COUNTER_NAMES = MetricsUtil.absoluteMetricNames(MonotonicCountedClassBean.class, "monotonicCountedClass",
            METHOD_NAMES);

    private static final Set<String> COUNTER_NAMES = MetricsUtil.absoluteMetricNames(MonotonicCountedClassBean.class, "monotonicCountedClass",
            METHOD_NAMES, CONSTRUCTOR_NAME);

    private static final MetricFilter METHOD_COUNTERS = new MetricFilter() {
        @Override
        public boolean matches(String name, Metric metric) {
            return METHOD_COUNTER_NAMES.contains(name);
        }
    };

    private static final AtomicLong CONSTRUCTOR_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                // Test bean
                .addClasses(MonotonicCountedClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MonotonicCountedClassBean bean;

    @Test
    @InSequence(1)
    public void countedMethodsNotCalledYet() {
        assertThat("Counters are not registered correctly", registry.getCounters().keySet(), is(equalTo(COUNTER_NAMES)));

        assertThat("Constructor timer count is incorrect", registry.getCounters().get(CONSTRUCTOR_COUNTER_NAME).getCount(),
                is(equalTo(CONSTRUCTOR_COUNT.incrementAndGet())));

        // Make sure that the counters haven't been incremented
        assertThat("Method counter counts are incorrect", registry.getCounters(METHOD_COUNTERS).values(),
                everyItem(Matchers.<Counter> hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callCountedMethodsOnce() {
        assertThat("Counters are not registered correctly", registry.getCounters().keySet(), is(equalTo(COUNTER_NAMES)));
        
        assertThat("Constructor timer count is incorrect", registry.getCounters().get(CONSTRUCTOR_COUNTER_NAME).getCount(),
                is(equalTo(CONSTRUCTOR_COUNT.incrementAndGet())));

        // Call the counted methods and assert they've been incremented
        bean.countedMethodOne();
        bean.countedMethodTwo();
        // Let's call the non-public methods as well
        bean.countedMethodProtected();
        bean.countedMethodPackagedPrivate();

        // Make sure that the counters have been incremented
        assertThat("Method counter counts are incorrect", registry.getCounters(METHOD_COUNTERS).values(),
                everyItem(Matchers.<Counter> hasProperty("count", equalTo(1L))));
    }
}