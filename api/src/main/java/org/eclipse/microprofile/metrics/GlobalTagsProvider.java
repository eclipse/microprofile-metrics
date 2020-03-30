/*
 **********************************************************************
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics;

import java.util.Set;

/**
 * This is an SPI that allows anyone to define global tags that
 * will be added to all the {@link org.eclipse.microprofile.metrics.Metric Metrics},
 * published by any {@link org.eclipse.microprofile.metrics.MetricRegistry metric registries}.
 * <p>
 * At runtime, all implementations of this interface will be discovered using the
 * {@link java.util.ServiceLoader ServiceLoader}, instantiated, and the result of
 * the {@link #getGlobalTags()} method will be added to a collection of global tags.
 * </p>
 * <p>
 * Note that this should only happen once, at application startup, after which point
 * the set of global tags will be immutable and can be cached to improve performance.
 * </p>
 */
public interface GlobalTagsProvider {
    /**
     * Return a set of global tags that should be added to each metric.
     *
     * @return a set of global tags that should be added to each metric
     */
    Set<? extends Tag> getGlobalTags();
}
