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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MetricProducerMethodBeanTest {

    private final static String CALLS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "calls");

    private final static String HITS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "hits");

    private final static String CACHE_HITS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "cache-hits");

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(MetricProducerMethodBean.class)
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MetricProducerMethodBean bean;

    @Test
    @InSequence(1)
    public void cachedMethodNotCalledYet() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(),
            allOf(
                hasKey(CALLS_METRIC),
                hasKey(HITS_METRIC),
                hasKey(CACHE_HITS_METRIC)
            )
        );
        Timer calls = registry.getTimers().get(CALLS_METRIC);
        Meter hits = registry.getMeters().get(HITS_METRIC);
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get(CACHE_HITS_METRIC);

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(((double) hits.getCount() / (double) calls.getCount()))));
    }

    @Test
    @InSequence(2)
    public void callCachedMethodMultipleTimes() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(),
            allOf(
                hasKey(CALLS_METRIC),
                hasKey(HITS_METRIC),
                hasKey(CACHE_HITS_METRIC)
            )
        );
        Timer calls = registry.getTimers().get(CALLS_METRIC);
        Meter hits = registry.getMeters().get(HITS_METRIC);
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get(CACHE_HITS_METRIC);

        long count = 10 + Math.round(Math.random() * 10);
        for (int i = 0; i < count; i++)
            bean.cachedMethod((Math.random() < 0.5));

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo((double) hits.getCount() / (double) calls.getCount())));
    }
}