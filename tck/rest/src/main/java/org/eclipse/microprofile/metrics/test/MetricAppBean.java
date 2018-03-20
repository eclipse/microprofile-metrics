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

package org.eclipse.microprofile.metrics.test;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetricAppBean {

    @Inject
    @Metric
    private Counter redCount;

    @Inject
    @Metric(name = "blue")
    private Counter blueCount;

    @Inject
    @Metric(absolute = true)
    private Counter greenCount;

    @Inject
    @Metric(name = "purple", absolute = true, tags = "app=myShop")
    private Counter purpleCount;

    @Inject
    @Metric(absolute = true, unit = "jellybeans")
    private Histogram jellybeanHistogram;

    @Inject
    // @RegistryType(type=MetricRegistry.Type.BASE)
    private MetricRegistry metrics;

    @Inject
    @ConfigProperty(name = "MP_METRICS_TAGS")
    private String globalTags;


    public void countMe() {
        Counter counter = metrics.counter("metricTest.test1.count");
        counter.inc();
    }

    @Counted(name = "metricTest.test1.countMeA", monotonic = true, absolute = true)
    public void countMeA() {

    }

    public void gaugeMe() {

        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = metrics.getGauges().get("metricTest.test1.gauge");
        if (gauge == null) {
            gauge = () -> {
                return 19L;
            };


            Metadata metadata = Metadata.builder().withName("metricTest.test1.gauge")
                    .withType(MetricType.GAUGE).withUnit(MetricUnits.GIGABYTES).build();
            metrics.register("metricTest.test1.gauge", gauge, metadata);
        }

    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(unit = MetricUnits.KIBIBITS)
    public long gaugeMeA() {
        return 1000L;
    }

    public void histogramMe() {

        Metadata metadata = Metadata.builder().withName("metricTest.test1.histogram")
                .withType(MetricType.HISTOGRAM).withUnit(MetricUnits.BYTES).build();
        Histogram histogram = metrics.histogram(metadata);

        for (int i = 0; i < 1000; i++) {
            histogram.update(i);
        }

        Metadata metadata2 = Metadata.builder().withName("metricTest.test1.histogram2")
                .withType(MetricType.HISTOGRAM).withUnit(MetricUnits.NONE).build();
        Histogram histogram2 = metrics.histogram(metadata2);
        histogram2.update(1);
    }

    public void meterMe() {

        Meter meter = metrics.meter("metricTest.test1.meter");
        meter.mark();

    }

    @Metered(absolute = true)
    public void meterMeA() {

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

    public String getGlobalTags() {
        return globalTags;
    }

    /**
     * We create a few metrics with names that are outside the
     * characters that prometheus allows which is [a-zA-Z0-9_]
     */
    public void createPromMetrics() {

        metrics.counter("pm_counter-with-dashes");

        metrics.counter("pm_counter#hash_x'y_");

        metrics.counter("pm_counter-umlaut-äöü");

        metrics.counter("pm_counter+accent_ê_");

    }
}
