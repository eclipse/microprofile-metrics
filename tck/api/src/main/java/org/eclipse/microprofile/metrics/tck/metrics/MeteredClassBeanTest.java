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
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.metrics.Meter;
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
public class MeteredClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "MeteredClassBean";

    private static final String CONSTRUCTOR_METER_NAME =
            MetricsUtil.absoluteMetricName(MeteredClassBean.class, "meteredClass", CONSTRUCTOR_NAME);

    private static MetricID constructorMID;

    private static final String[] METHOD_NAMES =
            {"meteredMethodOne", "meteredMethodTwo", "meteredMethodProtected", "meteredMethodPackagedPrivate"};

    private static final Set<String> METHOD_METER_NAMES =
            MetricsUtil.absoluteMetricNames(MeteredClassBean.class, "meteredClass", METHOD_NAMES);

    private static final MetricFilter METHOD_METERS = new MetricFilter() {
        @Override
        public boolean matches(MetricID metricID, Metric metric) {
            return METHOD_METER_NAMES.contains(metricID.getName());
        }
    };

    private static final Set<String> METER_NAMES =
            MetricsUtil.absoluteMetricNames(MeteredClassBean.class, "meteredClass", METHOD_NAMES,
                    CONSTRUCTOR_NAME);

    private static Set<MetricID> meterMIDs;

    private final static AtomicLong METHOD_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(MeteredClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        constructorMID = new MetricID(CONSTRUCTOR_METER_NAME);
        meterMIDs = MetricsUtil.createMetricIDs(METER_NAMES);
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MeteredClassBean bean;

    @Test
    @InSequence(1)
    public void meteredMethodsNotCalledYet() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(meterMIDs)));

        // Make sure that the method meters haven't been marked yet
        assertThat("Method meter counts are incorrect", registry.getMeters(METHOD_METERS).values(),
                everyItem(Matchers.<Meter>hasProperty("count", equalTo(METHOD_COUNT.get()))));

    }

    @Test
    @InSequence(2)
    public void callMeteredMethodsOnce() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(meterMIDs)));

        // Call the metered methods and assert they've been marked
        bean.meteredMethodOne();
        bean.meteredMethodTwo();
        // Let's call the non-public methods as well
        bean.meteredMethodProtected();
        bean.meteredMethodPackagedPrivate();

        // Make sure that the method meters have been marked
        assertThat("Method meter counts are incorrect", registry.getMeters(METHOD_METERS).values(),
                everyItem(Matchers.<Meter>hasProperty("count", equalTo(METHOD_COUNT.incrementAndGet()))));

        assertThat("Constructor's metric should be incremented at least once",
                registry.getMeter(constructorMID).getCount(), is(greaterThanOrEqualTo(1L)));
    }
}
