/*
 * ********************************************************************
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Timed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HistogramTimerConfigBean {

    @Inject
    private MetricRegistry applicationRegistry;

    @Timed(name = "annotatedTimerCustomPercentile", absolute = true)
    public void timeMeAnnotatedTimerCustomPercentile() {

    }

    @Timed(name = "annotatedTimerNoPercentile", absolute = true)
    public void timeMeAnnotatedTimerNoPercentile() {

    }

    @Timed(name = "annotatedTimerCustomBucketsDefaultPercentile", absolute = true)
    public void timeMeAnnotatedTimerCustomBucketsDefaultPercentile() {

    }

    @Timed(name = "annotatedTimerCustomBucketsCustomPercentile", absolute = true)
    public void timeMeAnnotatedTimerCustomBucketsCustomPercentile() {

    }

    @Timed(name = "annotatedTimerCustomBucketsNoPercentile", absolute = true)
    public void timeMeAnnotatedTimerCustomBucketsNoPercentile() {

    }

    public void programmaticHistograms() {
        applicationRegistry.histogram("histogramCustomPercentile");
        applicationRegistry.histogram("histogramNoPercentile");

        applicationRegistry.histogram("histogramCustomBucketsDefaultPercentile");
        applicationRegistry.histogram("histogramCustomBucketsCustomPercentile");
        applicationRegistry.histogram("histogramCustomBucketsNoPercentile");

    }

    public void programmaticTimers() {
        applicationRegistry.timer("timerCustomPercentile");
        applicationRegistry.timer("timerNoPercentile");

        applicationRegistry.timer("timerCustomBucketsDefaultPercentile");
        applicationRegistry.timer("timerCustomBucketsCustomPercentile");
        applicationRegistry.timer("timerCustomBucketsNoPercentile");
    }

    public void programmaticBadConfigs() {
        applicationRegistry.timer("timerBadPercentiles");
        applicationRegistry.timer("timerBadBuckets");

        applicationRegistry.histogram("histogramBadPercentiles");
        applicationRegistry.histogram("histogramBadBuckets");
    }

    public void precedence() {
        applicationRegistry.histogram("precedence.histogram");
        applicationRegistry.histogram("precedence.override.histogram");

        applicationRegistry.timer("precedence.timer");
        applicationRegistry.timer("precedence.override.timer");
    }

}
