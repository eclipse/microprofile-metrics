/*
 * ********************************************************************
 *  Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 *  See the NOTICES file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ********************************************************************
 *
 */

package org.eclipse.microprofile.metrics.test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

@ApplicationScoped
public class MetricAppBean2 {

    @Inject
    private MetricRegistry registry;

    @Counted(name = "countMe2", absolute = true, reusable = true)
    public void countMeA() {

    }

    @Counted(name = "countMe2", absolute = true, reusable = true)
    public void countMeB() {

    }

    @Metered(reusable = true, name = "meterMe2")
    public void meterMeA() {

    }

    @Metered(reusable = true, name = "meterMe2")
    public void meterMeB() {

    }

    @Timed(absolute = true, reusable = true, name = "timeMe2")
    public void timeMeA() {

    }
    @Timed(absolute = true, reusable = true, name = "timeMe2")
    public void timeMeB() {

    }

    @ConcurrentGauge
    public void concGaugeMeA() {

    }


    public void registerReusableHistogram() {

        Metadata metadata = Metadata.builder()
            .withName("reusableHisto").withType(MetricType.HISTOGRAM)
            .reusable().build();
        Histogram histogram = registry.histogram(metadata);

        histogram.update(1);

        Histogram histogram1 = registry.histogram(metadata);

        histogram1.update(3);
    }


    public void badRegisterReusableMixed() {

        Metadata metadata = Metadata.builder()
            .withName("badReusableMixed").withType(MetricType.HISTOGRAM)
            .reusable().build();
        Histogram histogram = registry.histogram(metadata);

        histogram.update(1);

        // We register a different metric type - that is forbidden
        // so we expect an exception

        Metadata metadata2 = Metadata.builder()
            .withName("badReusableMixed").withType(MetricType.COUNTER)
            .reusable().build();
        registry.counter(metadata2);

    }

}
