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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Snapshot.HistogramBucket;
import org.eclipse.microprofile.metrics.Snapshot.PercentileValue;
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
 * See
 * /microprofile-metrics-api-tck/src/main/resources/META-INF/microprofile-config-histogramConfigFieldBeanTest.properties
 * for the MP Config property configuration
 */
public class HistogramConfigFieldBeanTest {

    private final static String HISTOGRAM_NAME_1 = MetricRegistry.name(HistogramConfigFieldBean.class,
            "injectedHistogramCustomPercentiles");
    private final static String HISTOGRAM_NAME_2 = MetricRegistry.name(HistogramConfigFieldBean.class,
            "injectedHistogramNoPercentiles");
    private final static String HISTOGRAM_NAME_3 = MetricRegistry.name(HistogramConfigFieldBean.class,
            "injectedHistogramCustomBucketsDefaultPercentiles");
    private final static String HISTOGRAM_NAME_4 = MetricRegistry.name(HistogramConfigFieldBean.class,
            "injectedHistogramCustomBucketsCustomPercentiles");
    private final static String HISTOGRAM_NAME_5 = MetricRegistry.name(HistogramConfigFieldBean.class,
            "injectedHistogramCustomBucketsNoPercentiles");

    private final static String HISTOGRAM_NAME_6 = "injected.precedence.histogram";

    private final static String HISTOGRAM_NAME_7 = "injected.precedence.override.histogram";

    // private static MetricID histogramMID1, histogramMID2, histogramMID3, histogramMID4, histogramMID5, histogramMID6,
    // histogramMID7;

    @Deployment
    static Archive<?> createTestArchive() {
        WebArchive jar = ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(HistogramConfigFieldBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
                .addAsManifestResource("META-INF/microprofile-config-histogramConfigFieldBeanTest.properties",
                        "microprofile-config.properties");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private HistogramConfigFieldBean bean;

    @Test
    public void checkHistogramCustomPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_1));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.4), equalTo(0.2)));
    }

    @Test
    public void checkHistogramNoPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_2));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(0));

    }

    @Test
    public void checkHistogramCustomBucketsDefaultPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_3));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(6));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.5), equalTo(0.75), equalTo(0.95), equalTo(0.98), equalTo(0.99),
                        equalTo(0.999)));

        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(100.0), equalTo(200.0)));
    }

    @Test
    public void checkHistogramCustomBucketsCustomPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_4));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.7), equalTo(0.8)));

        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(150.0), equalTo(120.0)));
    }

    @Test
    public void checkHistogramCustomBucketsNoPercentiles() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_5));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(0));

        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(444.0), equalTo(555.0)));
    }

    @Test
    public void checkPrecedenceHistogram() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_6));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(2));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                containsInAnyOrder(equalTo(0.9), equalTo(0.8)));

        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(2));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                containsInAnyOrder(equalTo(23.0), equalTo(45.0)));
    }

    @Test
    public void checkPrecedenceOverrideHistogram() {
        Histogram histogram = registry.getHistogram(new MetricID(HISTOGRAM_NAME_7));
        PercentileValue[] percentiles = histogram.getSnapshot().percentileValues();

        assertThat("Configured percentiles length do not match", percentiles.length, equalTo(1));

        List<Double> percentilesList = Stream.of(percentiles).map(pv -> pv.getPercentile())
                .collect(Collectors.toList());

        assertThat("Configured percentiles do not match", percentilesList,
                contains(equalTo(0.2)));

        HistogramBucket[] buckets = histogram.getSnapshot().bucketValues();

        assertThat("Configured buckets length do not match", buckets.length, equalTo(1));

        List<Double> bucketsList = Stream.of(buckets).map(bucket -> bucket.getBucket())
                .collect(Collectors.toList());

        assertThat("Configured buckets do not match", bucketsList,
                contains(equalTo(32.0)));
    }
}
