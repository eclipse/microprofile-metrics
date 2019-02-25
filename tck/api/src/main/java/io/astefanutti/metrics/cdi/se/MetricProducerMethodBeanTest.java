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
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Meter;
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
public class MetricProducerMethodBeanTest {

    private final static String CALLS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "calls");

    private static MetricID callsMID; 
    
    private final static String HITS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "hits");

    private static MetricID hitsMID; 
    
    private final static String CACHE_HITS_METRIC = MetricRegistry.name(MetricProducerMethodBean.class, "cache-hits");
    
    private static MetricID cacheHitsMID; 

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(MetricProducerMethodBean.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MetricProducerMethodBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean explicitly
        // as only a proxy gets injected otherwise
        bean.toString();
        /*
         * The MetricID relies on the MicroProfile Config API.
         * Running a managed arquillian container will result
         * with the MetricID being created in a client process
         * that does not contain the MPConfig impl.
         * 
         * This will cause client instantiated MetricIDs to 
         * throw an exception. (i.e the global MetricIDs)
         */
        callsMID = new MetricID(CALLS_METRIC); 
        hitsMID = new MetricID(HITS_METRIC); 
        cacheHitsMID = new MetricID(CACHE_HITS_METRIC); 
    }

    @Test
    @InSequence(1)
    public void cachedMethodNotCalledYet() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(),
            allOf(
                hasKey(callsMID),
                hasKey(hitsMID),
                hasKey(cacheHitsMID)
            )
        );
        Timer calls = registry.getTimers().get(callsMID);
        Meter hits = registry.getMeters().get(hitsMID);
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get(cacheHitsMID);

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(((double) hits.getCount() / (double) calls.getCount()))));
    }

    @Test
    @InSequence(2)
    public void callCachedMethodMultipleTimes() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(),
            allOf(
                hasKey(callsMID),
                hasKey(hitsMID),
                hasKey(cacheHitsMID)
            )
        );
        Timer calls = registry.getTimers().get(callsMID);
        Meter hits = registry.getMeters().get(hitsMID);
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get(cacheHitsMID);

        long count = 10 + Math.round(Math.random() * 10);
        for (int i = 0; i < count; i++) {
            bean.cachedMethod((Math.random() < 0.5));
        }

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo((double) hits.getCount() / (double) calls.getCount())));
    }
}