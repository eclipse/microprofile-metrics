/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MetricRegistryTest {

    @Inject
    @Metric(name = "nameTest", absolute = true)
    private Counter nameTest;

    @Inject
    private Counter countTemp;

    @Inject
    private Histogram histoTemp;

    @Inject
    private Timer timerTemp;

    @Inject
    private Meter meterTemp;

    @Inject
    private MetricRegistry metrics;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @InSequence(1)
    public void nameTest() {
        Assert.assertNotNull(metrics);
        Assert.assertTrue(metrics.getNames().contains("nameTest"));
    }

    @Test
    @InSequence(2)
    public void registerTest() {
        metrics.register("regCountTemp", countTemp);
        Assert.assertTrue(metrics.getCounters().containsKey(new MetricID("regCountTemp")));

        metrics.register("regHistoTemp", histoTemp);
        Assert.assertTrue(metrics.getHistograms().containsKey(new MetricID("regHistoTemp")));

        metrics.register("regTimerTemp", timerTemp);
        Assert.assertTrue(metrics.getTimers().containsKey(new MetricID("regTimerTemp")));

        metrics.register("regMeterTemp", meterTemp);
        Assert.assertTrue(metrics.getMeters().containsKey(new MetricID("regMeterTemp")));
    }

    @Test
    @InSequence(3)
    public void removeTest() {
        metrics.remove("nameTest");
        Assert.assertFalse(metrics.getNames().contains("nameTest"));
    }
    
    @Test
    @InSequence(4)
    public void useExistingMetaDataTest() {
        String displayName = "displayCounterFoo";
        String metricName = "counterFoo";
        
        //first to register a "complex" metadata
        metrics.counter(Metadata.builder().withName(metricName).withDisplayName(displayName).withType(MetricType.COUNTER).build());    
        
        Tag purpleTag = new Tag("colour","purple");
        //creates with a simple/template metadata or uses an existing one.
        metrics.counter(metricName, purpleTag);
        
        //check both counters have been registered
        Assert.assertTrue(metrics.getCounters().containsKey(new MetricID(metricName)));
        Assert.assertTrue(metrics.getCounters().containsKey(new MetricID(metricName, purpleTag)));
        
        //check that the "original" metadata wasn't replaced by the empty default metadata
        Assert.assertEquals(metrics.getMetadata().get(metricName).getDisplayName(), displayName);
    }

}
