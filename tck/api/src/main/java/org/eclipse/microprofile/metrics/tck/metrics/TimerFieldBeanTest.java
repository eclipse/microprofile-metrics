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

import java.util.Arrays;
import java.util.Set;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.tck.util.MetricsUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class TimerFieldBeanTest {

    private final static String[] METRIC_NAMES =
            {"timerWithoutAnnotation", "timerWithExplicitNonAbsoluteName", "timerWithNoName", "timerName"};

    private final static String[] ABSOLUTE_METRIC_NAMES = {"timerWithAbsoluteDefaultName", "timerAbsoluteName"};

    private Set<String> metricNames() {
        Set<String> names = MetricsUtil.absoluteMetricNames(TimerFieldBean.class, METRIC_NAMES);
        names.addAll(Arrays.asList(ABSOLUTE_METRIC_NAMES));
        return names;
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(TimerFieldBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimerFieldBean bean;

    @Test
    public void timerFieldsWithDefaultNamingConvention() {
        assertThat("Timers are not registered correctly", registry.getMetricIDs(),
                is(equalTo(MetricsUtil.createMetricIDs(metricNames()))));
    }
}
