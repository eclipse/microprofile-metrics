/*
 **********************************************************************
 * Copyright (c) 2017, 2018 Contributors to the Eclipse Foundation
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

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TagsTest {
    
    @Inject
    private MetricRegistry registry;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @InSequence(1)
    public void simpleTagTest() {
        Tag one = new Tag("hello", "world");
        Tag two = new Tag("goodbye", "friend");
        MetricID metricID = new MetricID("metricName", one, two);
        
        assertThat(metricID.getTags(), hasKey("hello"));
        assertThat(metricID.getTags(), hasValue("world"));
        
        assertThat(metricID.getTags(), hasKey("goodbye"));
        assertThat(metricID.getTags(), hasValue("friend"));
        
        //MP_METRICS_TAGS=tier=integration
        assertThat("Counter's Global Tag not set properly", metricID.getTags(), hasKey("tier"));
        assertThat("Counter's Global Tag not set properly", metricID.getTags(), hasValue("integration"));
    }

    @Test
    @InSequence(2)
    public void lastTagValueTest() {
        
        Tag tagColour = new Tag("colour","red");
        Tag tagColourTwo = new Tag("colour","blue");
        
        String counterName = "org.eclipse.microprofile.metrics.tck.TagTest.counter";
        Counter counter = registry.counter(counterName, tagColour, tagColourTwo);
        
        //MetricID that only has colour=blue... the last tag value to be passed in
        MetricID counterMID = new MetricID(counterName, tagColourTwo);
        
        //check the metric is registered
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterMID));
    }
    
    @Test
    @InSequence(3)
    public void counterTagsTest() {
        
        Tag tagEarth = new Tag("planet", "earth");
        Tag tagRed = new Tag("colour", "red");
        Tag tagBlue = new Tag("colour", "blue");
        
        String counterName = "org.eclipse.microprofile.metrics.tck.TagTest.counterColour";
        
        Counter counterColour = registry.counter(counterName);
        Counter counterRed = registry.counter(counterName,tagEarth,tagRed);
        Counter counterBlue = registry.counter(counterName,tagEarth,tagBlue);
        
        MetricID counterColourMID = new MetricID(counterName);
        MetricID counterRedMID = new MetricID(counterName, tagEarth,tagRed);
        MetricID counterBlueMID = new MetricID(counterName, tagEarth,tagRed);
        
        //check multi-dimensional metrics are registered
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterColourMID));
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterRedMID));
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterBlueMID));
    }
    
    @Test
    @InSequence(4)
    public void meterTagsTest() {
        
        Tag tagEarth = new Tag("planet", "earth");
        Tag tagRed = new Tag("colour", "red");
        Tag tagBlue = new Tag("colour", "blue");
        
        String meterName = "org.eclipse.microprofile.metrics.tck.TagTest.meterColour";
        
        Meter meterColour = registry.meter(meterName);
        Meter meterRed = registry.meter(meterName,tagEarth,tagRed);
        Meter meterBlue = registry.meter(meterName,tagEarth,tagBlue);
        
        MetricID meterColourMID = new MetricID(meterName);
        MetricID meterRedMID = new MetricID(meterName, tagEarth,tagRed);
        MetricID meterBlueMID = new MetricID(meterName, tagEarth,tagRed);
        
        //check multi-dimensional metrics are registered
        assertThat("Meter is not registered correctly", registry.getMeters(), hasKey(meterColourMID));
        assertThat("Meter is not registered correctly", registry.getMeters(), hasKey(meterRedMID));
        assertThat("Meter is not registered correctly", registry.getMeters(), hasKey(meterBlueMID));
    }
    
    @Test
    @InSequence(5)
    public void timerTagsTest() {
        
        Tag tagEarth = new Tag("planet", "earth");
        Tag tagRed = new Tag("colour", "red");
        Tag tagBlue = new Tag("colour", "blue");
        
        String timerName = "org.eclipse.microprofile.metrics.tck.TagTest.timerColour";
        
        Timer timerColour = registry.timer(timerName);
        Timer timerRed = registry.timer(timerName,tagEarth,tagRed);
        Timer timerBlue = registry.timer(timerName,tagEarth,tagBlue);
        
        MetricID timerColourMID = new MetricID(timerName);
        MetricID timerRedMID = new MetricID(timerName, tagEarth,tagRed);
        MetricID timerBlueMID = new MetricID(timerName, tagEarth,tagRed);
        
        //check multi-dimensional metrics are registered
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerColourMID));
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerRedMID));
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerBlueMID));
    }
    
    @Test
    @InSequence(6)
    public void histogramTagsTest() {
        
        Tag tagEarth = new Tag("planet", "earth");
        Tag tagRed = new Tag("colour", "red");
        Tag tagBlue = new Tag("colour", "blue");
        
        String histogramName = "org.eclipse.microprofile.metrics.tck.TagTest.histogramColour";
        
        Histogram histogramColour = registry.histogram(histogramName);
        Histogram histogramRed = registry.histogram(histogramName,tagEarth,tagRed);
        Histogram histogramBlue = registry.histogram(histogramName,tagEarth,tagBlue);
        
        MetricID histogramColourMID = new MetricID(histogramName);
        MetricID histogramRedMID = new MetricID(histogramName, tagEarth,tagRed);
        MetricID histogramBlueMID = new MetricID(histogramName, tagEarth,tagRed);
        
        //check multi-dimensional metrics are registered
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(histogramColourMID));
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(histogramRedMID));
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(histogramBlueMID));
    }
}
