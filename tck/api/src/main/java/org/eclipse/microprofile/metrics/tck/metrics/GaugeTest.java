/*
 **********************************************************************
 * Copyright (c) 2017, 2022 Contributors to the Eclipse Foundation
 *               2010-2013 Coda Hale, Yammer.com
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

package org.eclipse.microprofile.metrics.tck.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class GaugeTest {

    @Inject
    private MetricRegistry metrics;

    private final AtomicInteger value = new AtomicInteger(0);

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    public void testManualGauge() {
        MetricID supplierGaugeMetricID = new MetricID("tck.gaugetest.supplierGaugeManual");
        MetricID toDoubleFunctiongaugeMetricID = new MetricID("tck.gaugetest.toDoubleFunctionGaugeManual");

        Gauge<?> sGauge = metrics.getGauge(supplierGaugeMetricID);
        Gauge<?> fGauge = metrics.getGauge(toDoubleFunctiongaugeMetricID);

        Assert.assertNull(sGauge);
        Assert.assertNull(fGauge);

        gaugeMe();

        sGauge = metrics.getGauge(supplierGaugeMetricID);
        fGauge = metrics.getGauge(toDoubleFunctiongaugeMetricID);

        Assert.assertEquals(0, sGauge.getValue());
        Assert.assertEquals(1, fGauge.getValue());

        Assert.assertEquals(2, sGauge.getValue());
        Assert.assertEquals(3, fGauge.getValue());
    }

    public void gaugeMe() {
        metrics.gauge("tck.gaugetest.supplierGaugeManual", value::getAndIncrement, null);

        metrics.gauge("tck.gaugetest.toDoubleFunctionGaugeManual", value,
                (atomicInteger) -> {
                    AtomicInteger myAtomicInteger = (AtomicInteger) atomicInteger;
                    return myAtomicInteger.getAndIncrement();
                }, null);

    }

}
