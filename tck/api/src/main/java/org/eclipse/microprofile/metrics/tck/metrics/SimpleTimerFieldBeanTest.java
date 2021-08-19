/*
 * ********************************************************************
 *  Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 *  See the NOTICES file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ********************************************************************
 *
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
public class SimpleTimerFieldBeanTest {

    private final static String[] METRIC_NAMES =
            {"simpleTimerWithoutAnnotation", "simpleTimerWithExplicitNonAbsoluteName",
                    "simpleTimerWithNoName", "simpleTimerName"};

    private final static String[] ABSOLUTE_METRIC_NAMES =
            {"simpleTimerWithAbsoluteDefaultName", "simpleTimerAbsoluteName"};

    private Set<String> metricNames() {
        Set<String> names = MetricsUtil.absoluteMetricNames(SimpleTimerFieldBean.class, METRIC_NAMES);
        names.addAll(Arrays.asList(ABSOLUTE_METRIC_NAMES));
        return names;
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(SimpleTimerFieldBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private SimpleTimerFieldBean bean;

    @Test
    public void simpleTimerFieldsWithDefaultNamingConvention() {
        assertThat("SimpleTimers are not registered correctly", registry.getMetricIDs(),
                is(equalTo(MetricsUtil.createMetricIDs(metricNames()))));
    }
}
