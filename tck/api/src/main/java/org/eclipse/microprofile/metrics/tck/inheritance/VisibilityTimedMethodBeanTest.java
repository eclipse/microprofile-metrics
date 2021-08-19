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
package org.eclipse.microprofile.metrics.tck.inheritance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

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
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class VisibilityTimedMethodBeanTest {

    private static final String[] TIMER_NAMES =
            {"publicTimedMethod", "packagePrivateTimedMethod", "protectedTimedMethod"};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNames(VisibilityTimedMethodBean.class, TIMER_NAMES);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(VisibilityTimedMethodBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private VisibilityTimedMethodBean bean;

    @Test
    @InSequence(1)
    public void timedMethodsNotCalledYet() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(),
                is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));

        // Make sure that all the timers haven't been called yet
        assertThat("Timer counts are incorrect", registry.getTimers().values(),
                everyItem(Matchers.<Timer>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodsOnce() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(),
                is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));

        // Call the timed methods and assert they've all been timed once
        bean.publicTimedMethod();
        bean.protectedTimedMethod();
        bean.packagePrivateTimedMethod();

        assertThat("Timer counts are incorrect", registry.getTimers().values(),
                everyItem(Matchers.<Timer>hasProperty("count", equalTo(1L))));
    }
}
