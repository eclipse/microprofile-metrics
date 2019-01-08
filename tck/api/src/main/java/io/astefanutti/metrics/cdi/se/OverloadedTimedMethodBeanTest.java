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
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

@RunWith(Arquillian.class)
public class OverloadedTimedMethodBeanTest {

    private final static String[] TIMER_NAMES = { "overloadedTimedMethodWithNoArguments", "overloadedTimedMethodWithStringArgument",
            "overloadedTimedMethodWithListOfStringArgument", "overloadedTimedMethodWithObjectArgument" };

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNames(OverloadedTimedMethodBean.class, TIMER_NAMES);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(OverloadedTimedMethodBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private OverloadedTimedMethodBean bean;

    @Test
    @InSequence(1)
    public void overloadedTimedMethodNotCalledYet() {
        Assert.assertTrue("Metrics are not registered correctly", 
            registry.getMetrics().keySet().containsAll(MetricsUtil.createMetricIDs(absoluteMetricNames())));

        // Make sure that all the timers haven't been called yet
        assertThat("Timer counts are incorrect", registry.getTimers().values(), hasItem(Matchers.<Timer> hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callOverloadedTimedMethodOnce() {
        Assert.assertTrue("Metrics are not registered correctly", 
            registry.getMetrics().keySet().containsAll(MetricsUtil.createMetricIDs(absoluteMetricNames())));

        // Call the timed methods and assert they've all been timed once
        bean.overloadedTimedMethod();
        bean.overloadedTimedMethod("string");
        bean.overloadedTimedMethod(new Object());
        bean.overloadedTimedMethod(Arrays.asList("string1", "string2"));
        assertThat("Timer counts are incorrect", registry.getTimers().values(), hasItem(Matchers.<Timer> hasProperty("count", equalTo(1L))));
    }
}