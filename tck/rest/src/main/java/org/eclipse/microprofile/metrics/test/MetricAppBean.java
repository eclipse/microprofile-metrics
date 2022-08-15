/*
 **********************************************************************
 * Copyright (c) 2017, 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.metrics.test;

import java.util.function.Supplier;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricAppBean {

    @Inject
    @Metric(description = "red-description")
    private Counter redCount;

    @Inject
    @Metric(name = "blue")
    private Counter blueCount;

    public static final String NO_TAG_COUNTER = "noTagCounter";
    public static final String TAGGED_COUNTER = "taggedCounter";

    public static final String NO_TAG_HISTOGRAM = "noTagHistogram";
    public static final String TAGGED_HISTOGRAM = "taggedHistogram";

    public static final String NO_TAG_TIMER = "noTagTimer";
    public static final String TAGGED_TIMER = "taggedTimer";

    public static final String NO_TAG_GAUGE = "noTagGauge";
    public static final String TAGGED_GAUGE = "taggedGauge";

    public static final String SHARED_METRIC_NAME = "sharedMetricName";

    @Counted(name = SHARED_METRIC_NAME, absolute = true, scope = "customScopeA")
    public void countMeMetricNameScopeA() {

    }

    @Timed(name = SHARED_METRIC_NAME, absolute = true, scope = "customScopeB")
    public void timeMeMetricNameScopeB() {

    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(name = SHARED_METRIC_NAME, absolute = true, scope = "customScopeC", unit = "jelly")
    public long gaugeMeMetricNameScopeC() {
        return 123L;
    }

    @Inject
    @Metric(name = SHARED_METRIC_NAME, absolute = true, scope = "customScopeD", unit = "marshmallow")
    private Histogram histogramMetricNameScopeD;

    @Inject
    @Metric(name = "semiColonTaggedCounter", tags = {"scTag=semi;colons;are;bad"})
    private Counter semiColonTaggedCounter;

    @Inject
    @Metric(name = NO_TAG_COUNTER)
    private Counter counterNoTag;

    @Inject
    @Metric(name = TAGGED_COUNTER, tags = {"number=one"})
    private Counter counterNumberOneTag;

    @Inject
    @Metric(name = TAGGED_COUNTER, tags = {"number=two"})
    private Counter counterNumberTwoTag;

    @Inject
    @Metric(name = NO_TAG_HISTOGRAM, absolute = true, unit = "marshmallow")
    private Histogram histogramNoTag;

    @Inject
    @Metric(name = TAGGED_HISTOGRAM, absolute = true, unit = "marshmallow", tags = {"number=one"})
    private Histogram histogramOneTag;

    @Inject
    @Metric(name = TAGGED_HISTOGRAM, absolute = true, unit = "marshmallow", tags = {"number=two"})
    private Histogram histogramTwoTag;

    @Inject
    @Metric(name = NO_TAG_TIMER, absolute = true)
    private Timer timerNoTag;

    @Inject
    @Metric(name = TAGGED_TIMER, absolute = true, tags = {"number=one"})
    private Timer timerOneTag;

    @Inject
    @Metric(name = TAGGED_TIMER, absolute = true, tags = {"number=two"})
    private Timer timerTwoTag;

    @org.eclipse.microprofile.metrics.annotation.Gauge(name = NO_TAG_GAUGE, absolute = true, unit = MetricUnits.NONE)
    public long gaugeMeTagged() {
        return 1000L;
    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(name = TAGGED_GAUGE, absolute = true, unit = MetricUnits.NONE, tags = {
            "number=one"})
    public long gaugeMeTaggedOne() {
        return 1000L;
    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(name = TAGGED_GAUGE, absolute = true, unit = MetricUnits.NONE, tags = {
            "number=two"})
    public long gaugeMeTaggedTwo() {
        return 1000L;
    }

    @Inject
    @Metric(absolute = true)
    private Counter greenCount;

    @Inject
    @Metric(name = "purple", absolute = true, tags = "app=myShop")
    private Counter purpleCount;

    @Inject
    @Metric(absolute = true, unit = "jellybeans", description = "jellybeans-description")
    private Histogram jellybeanHistogram;

    @Inject
    private MetricRegistry metrics;

    public void countMe() {
        Counter counter = metrics.counter("metricTest.test1.count");
        counter.inc();
    }

    @Counted(name = "metricTest.test1.countMeA", absolute = true, description = "count-me-a-description")
    public void countMeA() {

    }

    @Counted(name = "metricTest.test1.countMeB", absolute = true, unit = "jellybean")
    public long countMeB() {
        return 666666;
    }

    public void gaugeMe() {

        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = (Gauge<Long>) metrics.getGauge(new MetricID("metricTest.test1.gauge"));

        if (gauge == null) {

            Supplier<Long> gaugeSupp = () -> {
                return 19L;
            };

            Metadata metadata = Metadata.builder().withName("metricTest.test1.gauge")
                    .withType(MetricType.GAUGE).withUnit(MetricUnits.GIGABYTES).build();
            metrics.gauge(metadata, gaugeSupp);
        }

    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(unit = MetricUnits.KIBIBITS, description = "gauge-me-a-description")
    public long gaugeMeA() {
        return 1000L;
    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(unit = "hands")
    public long gaugeMeB() {
        return 7777777;
    }

    public void histogramMe() {

        Metadata metadata = Metadata.builder().withName("metricTest.test1.histogram")
                .withType(MetricType.HISTOGRAM).withUnit(MetricUnits.BYTES).build();
        Histogram histogram = metrics.histogram(metadata);

        // Go both ways to minimize error due to decay
        for (int i = 0; i < 500; i++) {
            histogram.update(i);
            histogram.update(999 - i);
        }

        Metadata metadata2 = Metadata.builder().withName("metricTest.test1.histogram2")
                .withType(MetricType.HISTOGRAM).withUnit(MetricUnits.NONE).build();
        Histogram histogram2 = metrics.histogram(metadata2);
        histogram2.update(1);
    }

    public void timeMe() {

        Timer timer = metrics.timer("metricTest.test1.timer");

        Timer.Context context = timer.time();
        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
        } finally {
            context.stop();
        }

    }

    @Timed
    public void timeMeA() {

    }

    /**
     * We create a few metrics with names that are outside the characters that OpenMetrics allows which is [a-zA-Z0-9_]
     */
    public void createPromMetrics() {

        metrics.counter("pm_counter-with-dashes");

        metrics.counter("pm_counter#hash_x'y_");

        metrics.counter("pm_counter-umlaut-äöü");

        metrics.counter("pm_counter+accent_ê_");

    }
}
