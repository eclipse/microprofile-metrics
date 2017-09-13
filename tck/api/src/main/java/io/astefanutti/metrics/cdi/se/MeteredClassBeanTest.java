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

import org.eclipse.microprofile.metrics.Meter;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

@RunWith(Arquillian.class)
public class MeteredClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "MeteredClassBean";

    private static final String CONSTRUCTOR_METER_NAME = MetricsUtil.absoluteMetricName(MeteredClassBean.class, "meteredClass", CONSTRUCTOR_NAME);

    private static final String[] METHOD_NAMES = { "meteredMethodOne", "meteredMethodTwo", "meteredMethodProtected", "meteredMethodPackagedPrivate" };

    private static final Set<String> METHOD_METER_NAMES = MetricsUtil.absoluteMetricNames(MeteredClassBean.class, "meteredClass", METHOD_NAMES);

    private static final MetricFilter METHOD_METERS = new MetricFilter() {
        @Override
        public boolean matches(String name, Metric metric) {
            return METHOD_METER_NAMES.contains(name);
        }
    };

    private static final Set<String> METER_NAMES = MetricsUtil.absoluteMetricNames(MeteredClassBean.class, "meteredClass", METHOD_NAMES,
            CONSTRUCTOR_NAME);

    private final static AtomicLong CONSTRUCTOR_COUNT = new AtomicLong();

    private final static AtomicLong METHOD_COUNT = new AtomicLong();

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                // Test bean
                .addClasses(MeteredClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MeteredClassBean bean;

    @Test
    @InSequence(1)
    public void meteredMethodsNotCalledYet() {
        Assert.assertTrue("Meters are not registered correctly", registry.getMeters().keySet().containsAll(METER_NAMES));

        assertThat("Constructor meter count is incorrect", registry.getMeters().get(CONSTRUCTOR_METER_NAME).getCount(),
                is(equalTo(CONSTRUCTOR_COUNT.incrementAndGet())));

        // Make sure that the method meters haven't been marked yet
        assertThat("Method meter counts are incorrect", registry.getMeters(METHOD_METERS).values(),
                everyItem(Matchers.<Meter> hasProperty("count", equalTo(METHOD_COUNT.get()))));
    }

    @Test
    @InSequence(2)
    public void callMeteredMethodsOnce() {
        Assert.assertTrue("Meters are not registered correctly", registry.getMeters().keySet().containsAll(METER_NAMES));

        assertThat("Constructor meter count is incorrect", registry.getMeters().get(CONSTRUCTOR_METER_NAME).getCount(),
                is(equalTo(CONSTRUCTOR_COUNT.incrementAndGet())));

        // Call the metered methods and assert they've been marked
        bean.meteredMethodOne();
        bean.meteredMethodTwo();
        // Let's call the non-public methods as well
        bean.meteredMethodProtected();
        bean.meteredMethodPackagedPrivate();

        // Make sure that the method meters have been marked
        assertThat("Method meter counts are incorrect", registry.getMeters(METHOD_METERS).values(),
                everyItem(Matchers.<Meter> hasProperty("count", equalTo(METHOD_COUNT.incrementAndGet()))));
    }
}