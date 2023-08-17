/**
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.metrics.tck.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Snapshot.HistogramBucket;
import org.eclipse.microprofile.metrics.Snapshot.PercentileValue;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
/*
 * See /microprofile-metrics-api-tck/src/main/resources/META-INF/microprofile-config-timerConfigFieldBeanTest.properties
 * for the MP Config property configuration
 */
public class TimerConfigFieldBeanTest {

    private final static String TIMER_NAME_1 = MetricRegistry.name(TimerConfigFieldBean.class,
            "injectedTimerCustomPercentiles");
    private final static String TIMER_NAME_2 = MetricRegistry.name(TimerConfigFieldBean.class,
            "injectedTimerNoPercentiles");
    private final static String TIMER_NAME_3 = MetricRegistry.name(TimerConfigFieldBean.class,
            "injectedTimerCustomBucketsDefaultPercentiles");
    private final static String TIMER_NAME_4 = MetricRegistry.name(TimerConfigFieldBean.class,
            "injectedTimerCustomBucketsCustomPercentiles");
    private final static String TIMER_NAME_5 = MetricRegistry.name(TimerConfigFieldBean.class,
            "injectedTimerCustomBucketsNoPercentiles");

    private final static String TIMER_NAME_6 = "injected.precedence.timer";

    private final static String TIMER_NAME_7 = "injected.precedence.override.timer";

    @Deployment
    static Archive<?> createTestArchive() {
        WebArchive jar = ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(TimerConfigFieldBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
                .addAsManifestResource("META-INF/microprofile-config-timerConfigFieldBeanTest.properties",
                        "microprofile-config.properties");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimerConfigFieldBean bean;

    @Test
    public void checkTimerCustomPercentiles() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_1));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.4), equalTo(0.2)));
    }

    @Test
    public void checkTimerNoPercentiles() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_2));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(0));

    }

    @Test
    public void checkTimerCustomBucketsDefaultPercentiles() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_3));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(6));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.5), equalTo(0.75), equalTo(0.95), equalTo(0.98), equalTo(0.99),
                        equalTo(0.999)));

        HistogramBucket[] buckets = timer.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(100L), equalTo(2000L)));
    }

    @Test
    public void checkTimerCustomBucketsCustomPercentiles() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_4));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.7), equalTo(0.8)));

        HistogramBucket[] buckets = timer.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(120L), equalTo(150L)));
    }

    @Test
    public void checkTimerCustomBucketsNoPercentiles() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_5));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(0));

        HistogramBucket[] buckets = timer.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(2000L), equalTo(555L)));
    }

    @Test
    public void checkPrecedenceTimer() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_6));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.9), equalTo(0.8)));

        HistogramBucket[] buckets = timer.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(23L), equalTo(455L)));
    }

    @Test
    public void checkPrecedenceOverrideTimer() {
        Timer timer = registry.getTimer(new MetricID(TIMER_NAME_7));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(1));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                contains(equalTo(0.2)));

        HistogramBucket[] buckets = timer.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(1));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                contains(equalTo(32000L)));
    }
}
