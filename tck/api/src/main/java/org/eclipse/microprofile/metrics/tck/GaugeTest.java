/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.metrics.tck;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GaugeTest {

    @Inject
    private MetricRegistry metrics;

    private final AtomicInteger value = new AtomicInteger(0);

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testManualGauge() {
        
        String gaugeName = "tck.gaugetest.gaugemanual";
        MetricID gaugeMetricID = new MetricID(gaugeName);
        
        Assert.assertNull(metrics.getGauges().get(gaugeMetricID));
        gaugeMe();

        Assert.assertEquals(0, (metrics.getGauges().get(gaugeMetricID).getValue()));
        Assert.assertEquals(1, (metrics.getGauges().get(gaugeMetricID).getValue()));
    }

    public void gaugeMe() {
        @SuppressWarnings("unchecked")
        Gauge<Integer> gaugeManual = metrics.getGauges().get("tck.gaugetest.gaugemanual");
        if (gaugeManual == null) {
            gaugeManual = value::getAndIncrement;
            metrics.register("tck.gaugetest.gaugemanual", gaugeManual);
        }
    }

}
