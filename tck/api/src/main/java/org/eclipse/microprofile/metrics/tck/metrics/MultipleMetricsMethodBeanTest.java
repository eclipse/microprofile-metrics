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
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.tck.util.MetricsUtil;
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
public class MultipleMetricsMethodBeanTest {

    private final static String[] METRIC_NAMES = {"counter", "gauge", "meter", "timer"};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNames(MultipleMetricsMethodBean.class, METRIC_NAMES);
    }

    private String absoluteMetricName(String name) {
        return MetricsUtil.absoluteMetricName(MultipleMetricsMethodBean.class, name);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(MultipleMetricsMethodBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MultipleMetricsMethodBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean
        // explicitly
        // as only a proxy gets injected otherwise
        bean.toString();
    }

    @Test
    @InSequence(1)
    public void metricsMethodNotCalledYet() {
        assertThat("Metrics are not registered correctly", registry.getMetricIDs(),
                is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));
    }

    @Test
    @InSequence(2)
    public void callMetricsMethodOnce() {
        assertThat("Metrics are not registered correctly", registry.getMetricIDs(),
                is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));

        // Call the monitored method and assert it's been instrumented
        bean.metricsMethod();

        // Make sure that the metrics have been called
        assertThat("Counter count is incorrect", registry.getCounter(
                new MetricID(absoluteMetricName("counter"))).getCount(), is(equalTo(1L)));
        assertThat("Meter count is incorrect", registry.getMeter(
                new MetricID(absoluteMetricName("meter"))).getCount(), is(equalTo(1L)));
        assertThat("Timer count is incorrect", registry.getTimer(
                new MetricID(absoluteMetricName("timer"))).getCount(), is(equalTo(1L)));
        // Let's call the gauge at the end as Weld is intercepting the gauge
        // invocation while OWB not
        assertThat("Gauge value is incorrect", registry.getGauge(
                new MetricID(absoluteMetricName("gauge"))).getValue(), is(equalTo(1234L)));
    }
}
