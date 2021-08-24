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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
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
public class CountedClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "CountedClassBean";

    private static final String CONSTRUCTOR_COUNTER_NAME =
            MetricsUtil.absoluteMetricName(CountedClassBean.class, "countedClass",
                    CONSTRUCTOR_NAME);

    private static MetricID constructorMID;

    private static final String[] METHOD_NAMES =
            {"countedMethodOne", "countedMethodTwo", "countedMethodProtected", "countedMethodPackagedPrivate"};

    private static final Set<String> METHOD_COUNTER_NAMES =
            MetricsUtil.absoluteMetricNames(CountedClassBean.class, "countedClass",
                    METHOD_NAMES);

    private static final Set<String> COUNTER_NAMES =
            MetricsUtil.absoluteMetricNames(CountedClassBean.class, "countedClass",
                    METHOD_NAMES, CONSTRUCTOR_NAME);

    private static Set<MetricID> counterMIDs;

    private static final MetricFilter METHOD_COUNTERS = new MetricFilter() {
        @Override
        public boolean matches(MetricID metricID, Metric metric) {
            return METHOD_COUNTER_NAMES.contains(metricID.getName());
        }
    };

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(CountedClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private CountedClassBean bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        constructorMID = new MetricID(CONSTRUCTOR_COUNTER_NAME);
        counterMIDs = MetricsUtil.createMetricIDs(COUNTER_NAMES);
    }

    @Test
    @InSequence(1)
    public void countedMethodsNotCalledYet() {
        assertThat("Counters are not registered correctly", registry.getCounters().keySet(), is(equalTo(counterMIDs)));

        // Make sure that the counters haven't been incremented
        assertThat("Method counter counts are incorrect", registry.getCounters(METHOD_COUNTERS).values(),
                everyItem(Matchers.<Counter>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callCountedMethodsOnce() {
        assertThat("Counters are not registered correctly", registry.getCounters().keySet(), is(equalTo(counterMIDs)));

        // Call the counted methods and assert they've been incremented
        bean.countedMethodOne();
        bean.countedMethodTwo();
        // Let's call the non-public methods as well
        bean.countedMethodProtected();
        bean.countedMethodPackagedPrivate();

        // Make sure that the counters have been incremented
        assertThat("Method counter counts are incorrect", registry.getCounters(METHOD_COUNTERS).values(),
                everyItem(Matchers.<Counter>hasProperty("count", equalTo(1L))));

        assertThat("Constructor's metric should be incremented at least once",
                registry.getCounter(constructorMID).getCount(), is(greaterThanOrEqualTo(1L)));
    }
}
