/*
 **********************************************************************
 * Copyright (c) 2017, 2019 Contributors to the Eclipse Foundation
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

import static org.junit.Assert.assertNotNull;

import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

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
    private SimpleTimer simpleTimerTemp;

    @Inject
    private ConcurrentGauge concurrentGaugeTemp;

    @Inject
    private Meter meterTemp;

    @Inject
    private MetricRegistry metrics;

    @Inject
    @RegistryType(type = MetricRegistry.Type.BASE)
    private MetricRegistry baseMetrics;

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    private MetricRegistry vendorMetrics;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @InSequence(1)
    public void nameTest() {
        Assert.assertNotNull(metrics);
        Assert.assertNotNull(metrics.getMetadata("nameTest"));
    }

    @Test
    @InSequence(2)
    public void registerTest() {
        metrics.register("regCountTemp", countTemp);
        assertExists(Counter.class, new MetricID("regCountTemp"));

        metrics.register("regHistoTemp", histoTemp);
        assertExists(Histogram.class, new MetricID("regHistoTemp"));

        metrics.register("regTimerTemp", timerTemp);
        assertExists(Timer.class, new MetricID("regTimerTemp"));

        metrics.register("regSimpleTimerTemp", simpleTimerTemp);
        assertExists(SimpleTimer.class, new MetricID("regSimpleTimerTemp"));

        metrics.register("regConcurrentGaugeTemp", concurrentGaugeTemp);
        assertExists(ConcurrentGauge.class, new MetricID("regConcurrentGaugeTemp"));

        metrics.register("regMeterTemp", meterTemp);
        assertExists(Meter.class, new MetricID("regMeterTemp"));
    }

    @Test
    @InSequence(3)
    public void removeTest() {
        metrics.remove("nameTest");
        Assert.assertNull(metrics.getMetadata("nameTest"));
    }

    @Test
    @InSequence(4)
    public void useExistingMetaDataTest() {
        String displayName = "displayCounterFoo";
        String metricName = "counterFoo";

        // first to register a "complex" metadata
        metrics.counter(Metadata.builder().withName(metricName).withDisplayName(displayName)
                .withType(MetricType.COUNTER).build());

        Tag purpleTag = new Tag("colour", "purple");
        // creates with a simple/template metadata or uses an existing one.
        metrics.counter(metricName, purpleTag);

        // check both counters have been registered
        assertExists(Counter.class, new MetricID(metricName));
        assertExists(Counter.class, new MetricID(metricName, purpleTag));

        // check that the "original" metadata wasn't replaced by the empty default metadata
        Assert.assertEquals(metrics.getMetadata(metricName).getDisplayName(), displayName);
    }

    @Test
    @InSequence(5)
    public void testMetricRegistryType() {
        Assert.assertEquals(MetricRegistry.Type.APPLICATION, metrics.getType());
        Assert.assertEquals(MetricRegistry.Type.BASE, baseMetrics.getType());
        Assert.assertEquals(MetricRegistry.Type.VENDOR, vendorMetrics.getType());
    }

    private void assertExists(Class<? extends org.eclipse.microprofile.metrics.Metric> expected, MetricID metricID) {
        assertNotNull("Metric expected to exist but was undefined: " + metricID, metrics.getMetric(metricID, expected));
    }

    /**
     * The implementation has to sanitize Metadata passed to registration methods if the application does not supply the
     * metric type explicitly, but the type is implied by the used method.
     */
    @Test
    @InSequence(6)
    public void sanitizeMetadataTest() {
        Metadata metadata = Metadata.builder().withName("metric1").build();
        metrics.counter(metadata);
        Metadata actualMetadata = metrics.getMetadata("metric1");
        Assert.assertEquals(MetricType.COUNTER, actualMetadata.getTypeRaw());
    }

    /**
     * if there is a mismatch because the type specified in the `Metadata` is different than the one implied by the
     * method name, an exception must be thrown
     */
    @Test(expected = Exception.class)
    @InSequence(7)
    public void conflictingMetadataTest() {
        Metadata metadata = Metadata.builder().withName("metric1").withType(MetricType.COUNTER).build();
        metrics.meter(metadata);
    }

}
