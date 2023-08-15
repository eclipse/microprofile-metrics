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

import org.eclipse.microprofile.metrics.Histogram;
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
 * See /microprofile-metrics-api-tck/src/main/resources/META-INF/microprofile-config-badHistogramTimerConfig.properties
 * for the MP Config propreties
 */
public class BadHistogramTimerConfigTest {

    @Deployment
    static Archive<?> createTestArchive() {
        WebArchive jar = ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(BadHistogramTimerConfigBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
                .addAsManifestResource("META-INF/microprofile-config-badHistogramTimerConfig.properties",
                        "microprofile-config.properties");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private BadHistogramTimerConfigBean bean;

    @Test
    public void checkBadHistogramPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID("badHistogramPercentiles"));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(3));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.1), equalTo(0.3), equalTo(0.4)));
    }

    @Test
    public void checkBadTimermPercentiles() {
        Timer timer = registry.getTimer(new MetricID("badTimerPercentiles"));
        PercentileValue[] percentiles = timer.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(1));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                contains(equalTo(0.1)));
    }

    @Test
    public void checkBadHistogramBuckets() {
        Histogram histogram = registry.getHistogram(new MetricID("badHistogramBuckets"));
        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(3));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(10.0), equalTo(12.0), equalTo(90.0)));
    }

    @Test
    public void checkBadTimerBuckets() {
        Timer histogram = registry.getTimer(new MetricID("badTimerBuckets"));
        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(3));

        // convert to milliseconds
        List<Long> bucketsList = Stream.of(buckets)
                .map(bucket -> TimeUnit.MILLISECONDS.convert((long) bucket.getBucket(), TimeUnit.NANOSECONDS))
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(10L), equalTo(30L), equalTo(500L)));
    }
}
