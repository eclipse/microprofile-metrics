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

package org.eclipse.microprofile.metrics.tck.namedmetadata;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.NamedMetadata;
import org.eclipse.microprofile.metrics.annotation.NamedMetadatas;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class NamedMetadataTest {

    @SuppressWarnings("DefaultAnnotationParam")
    @NamedMetadatas({
        @NamedMetadata(name = "my-counter-metadata",
            metricName = "my-counter",
            description = "awesome-counter-description",
            displayName = "awesome-counter-display-name",
            unit = "awesome-counter-unit",
            type = MetricType.COUNTER),
        @NamedMetadata(name = "my-meter-metadata",
            metricName = "my-meter",
            description = "awesome-meter-description",
            displayName = "awesome-meter-display-name",
            unit = "awesome-meter-unit",
            type = MetricType.METERED),
        @NamedMetadata(name = "my-timer-metadata",
            metricName = "my-timer",
            description = "awesome-timer-description",
            displayName = "awesome-timer-display-name",
            unit = "awesome-timer-unit",
            type = MetricType.TIMER),
        @NamedMetadata(name = "my-concurrent-gauge-metadata",
            metricName = "my-concurrent-gauge",
            description = "awesome-concurrent-gauge-description",
            displayName = "awesome-concurrent-gauge-display-name",
            unit = "awesome-concurrent-gauge-unit",
            type = MetricType.CONCURRENT_GAUGE)
    })
    public static class BeanWithMetadataAnnotation {

        @Counted(metadata = "my-counter-metadata")
        public void countedMethod() {

        }

        @Metered(metadata = "my-meter-metadata")
        public void meteredMethod() {

        }

        @Timed(metadata = "my-timer-metadata")
        public void timedMethod() {

        }

/*        @Gauge(metadata = "my-gauge-metadata")
        public Long gaugedMethod() {
            return 1234L;
        }*/

        @ConcurrentGauge(metadata = "my-concurrent-gauge-metadata")
        public void concurrentGaugedMethod(CountDownLatch startLatch, CountDownLatch finishLatch) {
            try {
                startLatch.countDown();
                finishLatch.await(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(BeanWithMetadataAnnotation.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    BeanWithMetadataAnnotation bean;

    @Inject
    MetricRegistry metricRegistry;

    @Test
    public void verifyCounter() {
        Metadata metadata = metricRegistry.getMetadata().get("my-counter");
        assertNotNull(metadata);
        assertEquals("awesome-counter-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-counter-display-name", metadata.getDisplayName());
        assertEquals("awesome-counter-unit", metadata.getUnit().orElse(""));

        bean.countedMethod();
        Counter counter = metricRegistry.getCounters().get(new MetricID("my-counter"));
        assertEquals(1, counter.getCount());
    }

    @Test
    public void verifyMeter() {
        Metadata metadata = metricRegistry.getMetadata().get("my-meter");
        assertNotNull(metadata);
        assertEquals("awesome-meter-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-meter-display-name", metadata.getDisplayName());
        assertEquals("awesome-meter-unit", metadata.getUnit().orElse(""));

        bean.meteredMethod();
        Meter meter = metricRegistry.getMeters().get(new MetricID("my-meter"));
        assertEquals(1, meter.getCount());
    }

    @Test
    public void verifyTimer() {
        Metadata metadata = metricRegistry.getMetadata().get("my-timer");
        assertNotNull(metadata);
        assertEquals("awesome-timer-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-timer-display-name", metadata.getDisplayName());
        assertEquals("awesome-timer-unit", metadata.getUnit().orElse(""));

        bean.timedMethod();
        Timer timer = metricRegistry.getTimers().get(new MetricID("my-timer"));
        assertEquals(1, timer.getCount());
    }

/*    @Test
    public void verifyGauge() {
        Metadata metadata = metricRegistry.getMetadata().get("my-gauge1");
        assertNotNull(metadata);
        assertEquals("awesome-gauge-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-gauge-display-name", metadata.getDisplayName());
        assertEquals("awesome-gauge-unit", metadata.getUnit().orElse(""));

        org.eclipse.microprofile.metrics.Gauge gauge = metricRegistry.getGauges().get(new MetricID("my-gauge1"));
        assertEquals(1234L, gauge.getValue());
    }*/

    @Test
    public void verifyConcurrentGauge() throws InterruptedException {
        Metadata metadata = metricRegistry.getMetadata().get("my-concurrent-gauge");
        assertNotNull(metadata);
        assertEquals("awesome-concurrent-gauge-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-concurrent-gauge-display-name", metadata.getDisplayName());
        assertEquals("awesome-concurrent-gauge-unit", metadata.getUnit().orElse(""));

        org.eclipse.microprofile.metrics.ConcurrentGauge cGauge = metricRegistry.getConcurrentGauges().get(new MetricID("my-concurrent-gauge"));
        CountDownLatch endLatch = new CountDownLatch(1);
        CountDownLatch startLatch = new CountDownLatch(1);
        new Thread(() -> {
            bean.concurrentGaugedMethod(startLatch, endLatch);
        }).start();
        try {
            startLatch.await(10, TimeUnit.SECONDS);
            assertEquals(1L, cGauge.getCount());
        } finally {
            endLatch.countDown();
        }
    }

}
