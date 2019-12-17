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
package org.eclipse.microprofile.metrics.tck.inheritance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.everyItem;

import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
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

@RunWith(Arquillian.class)
public class InheritedSimplyTimedMethodBeanTest {

    private static final String[] PARENT_SIMPLE_TIMER_NAMES = {"publicSimplyTimedMethod", "packagePrivateSimplyTimedMethod",
            "protectedSimplyTimedMethod"};
    private static final String[] CHILD_SIMPLE_TIMER_NAMES = {"simplyTimedMethodOne", "simplyTimedMethodTwo", "simplyTimedMethodProtected",
            "simplyTimedMethodPackagedPrivate"};

    private Set<String> absoluteMetricNames() {
        Set<String> names = MetricsUtil.absoluteMetricNames(VisibilitySimplyTimedMethodBean.class, PARENT_SIMPLE_TIMER_NAMES);
        names.addAll(MetricsUtil.absoluteMetricNames(InheritedSimplyTimedMethodBean.class, CHILD_SIMPLE_TIMER_NAMES));
        return names;
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClasses(VisibilitySimplyTimedMethodBean.class, InheritedSimplyTimedMethodBean.class, MetricsUtil.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private InheritedSimplyTimedMethodBean bean;

    @Test
    @InSequence(1)
    public void simplyTimedMethodsNotCalledYet() {
        assertThat("SimpleTimer are not registered correctly", registry.getSimpleTimers().keySet(),
            is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));

        // Make sure that all the timers haven't been called yet
        assertThat("SimpleTimer counts are incorrect", registry.getSimpleTimers().values(), 
                everyItem(Matchers.<SimpleTimer>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callSimplyTimedMethodsOnce() { 
        assertThat("SimpleTimer are not registered correctly", registry.getSimpleTimers().keySet(),
            is(equalTo(MetricsUtil.createMetricIDs(absoluteMetricNames()))));

        // Call the simplyTimed methods and assert they've all been simplyTimed once
        bean.publicSimplyTimedMethod();
        bean.protectedSimplyTimedMethod();
        bean.packagePrivateSimplyTimedMethod();

        // Call the methods of the parent and assert they've also been simplyTimed once
        bean.simplyTimedMethodOne();
        bean.simplyTimedMethodTwo();
        bean.simplyTimedMethodProtected();
        bean.simplyTimedMethodPackagedPrivate();

        assertThat("SimpleTimer counts are incorrect", registry.getSimpleTimers().values(),
                everyItem(Matchers.<SimpleTimer>hasProperty("count", equalTo(1L))));
    }
}
