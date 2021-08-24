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

package org.eclipse.microprofile.metrics.test.multipleinstances;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Validate that when metrics are used on beans that create multiple instances, the metric usages are merged over all
 * instances of that bean.
 */
@RunWith(Arquillian.class)
public class MultipleBeanInstancesTest {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class).addClass(DependentScopedBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private Instance<DependentScopedBean> bean;

    private DependentScopedBean instance1;

    private DependentScopedBean instance2;

    @Before
    public void getInstances() {
        instance1 = bean.get();
        instance2 = bean.get();
        assertFalse("CDI container should return two different bean instances",
                instance1.equals(instance2));
    }

    @Test
    public void testCounter() {
        instance1.countedMethod();
        instance2.countedMethod();
        assertThat(registry.getCounters((id, metric) -> id.getName().equals("counter")).values().iterator().next()
                .getCount(), is(2L));
    }

    @Test
    public void testMeter() {
        instance1.meteredMethod();
        instance2.meteredMethod();
        assertThat(
                registry.getMeters((id, metric) -> id.getName().equals("meter")).values().iterator().next().getCount(),
                is(2L));
    }

    @Test
    public void testTimer() {
        instance1.timedMethod();
        instance2.timedMethod();
        assertThat(
                registry.getTimers((id, metric) -> id.getName().equals("timer")).values().iterator().next().getCount(),
                is(2L));
    }

}
