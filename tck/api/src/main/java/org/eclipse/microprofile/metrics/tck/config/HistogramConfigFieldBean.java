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

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.annotation.Metric;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class HistogramConfigFieldBean {

    @Inject
    @Metric(name = "injectedHistogramCustomPercentiles")
    private Histogram injectedHistogramCustomPercentiles;

    @Inject
    @Metric(name = "injectedHistogramNoPercentiles")
    private Histogram injectedHistogramNoPercentiles;

    @Inject
    @Metric(name = "injectedHistogramCustomBucketsDefaultPercentiles")
    private Histogram injectedHistogramCustomBucketsDefaultPercentiles;

    @Inject
    @Metric(name = "injectedHistogramCustomBucketsCustomPercentiles")
    private Histogram injectedHistogramCustomBucketsCustomPercentiles;

    @Inject
    @Metric(name = "injectedHistogramCustomBucketsNoPercentiles")
    private Histogram injectedHistogramCustomBucketsNoPercentiles;

    @Inject
    @Metric(name = "injected.precedence.histogram", absolute = true)
    private Histogram injectedPrecedenceHistogram;

    @Inject
    @Metric(name = "injected.precedence.override.histogram", absolute = true)
    private Histogram injectedPrecedenceOverrideHistogram;

}
