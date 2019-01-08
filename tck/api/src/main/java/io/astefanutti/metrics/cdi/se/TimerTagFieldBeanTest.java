/*
 **********************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICES file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 **********************************************************************/
package io.astefanutti.metrics.cdi.se;

import javax.inject.Inject;

import static org.hamcrest.Matchers.hasKey;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

@RunWith(Arquillian.class)
public class TimerTagFieldBeanTest {

    private final static String TIMER_NAME = "timerName";

    private final static Tag NUMBER_ONE_TAG = new Tag("number", "one");
    private final static Tag NUMBER_TWO_TAG = new Tag("number", "two");
    
    private final static MetricID TIMER_ONE_METRICID = new MetricID(TIMER_NAME, NUMBER_ONE_TAG);
    private final static MetricID TIMER_TWO_METRICID = new MetricID(TIMER_NAME, NUMBER_TWO_TAG);

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClasses(TimerTagFieldBean.class, MetricsUtil.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimerTagFieldBean bean;
    
    @Test
    public void timersTagFieldRegistered() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_ONE_METRICID));
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_TWO_METRICID));
    }
}