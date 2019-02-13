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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
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
public class GaugeMethodBeanTest {

    private final static String GAUGE_NAME = MetricRegistry.name(GaugeMethodBean.class, "gaugeMethod");

    private static MetricID gaugeMID;
    
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(GaugeMethodBean.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private GaugeMethodBean bean;

    @Before
    public void instantiateApplicationScopedBeanAndTest() {
        // Let's trigger the instantiation of the application scoped bean explicitly
        // as only a proxy gets injected otherwise
        bean.getGauge();
        /*
         * The MetricID relies on the MicroProfile Config API.
         * Running a managed arquillian container will result
         * with the MetricID being created in a client process
         * that does not contain the MPConfig impl.
         * 
         * This will cause client instantiated MetricIDs to 
         * throw an exception. (i.e the global MetricIDs)
         */
         gaugeMID = new MetricID(GAUGE_NAME);
    }

    @Test
    @InSequence(1)
    public void gaugeCalledWithDefaultValue() {
        assertThat("Gauge is not registered correctly", registry.getGauges(), hasKey(gaugeMID));
        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = registry.getGauges().get(gaugeMID);

        // Make sure that the gauge has the expected value
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callGaugeAfterSetterCall() {
        assertThat("Gauge is not registered correctly", registry.getGauges(), hasKey(gaugeMID));
        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = registry.getGauges().get(gaugeMID);

        // Call the setter method and assert the gauge is up-to-date
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        bean.setGauge(value);
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(value)));
    }
}