/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *               2017 Red Hat, Inc. and/or its affiliates
 *               and other contributors as indicated by the @author tags.
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

import java.util.Map;
import java.util.Optional;

/**
 * Bean holding the metadata of one single metric.
 * <p>
 * The metadata contains:
 * <ul>
 * <li>
 * {@code Name}: (Required) The name of the metric.
 * </li>
 * <li>
 * {@code Display name}: (Optional) The display (friendly) name of the metric.
 * By default, it is set to the {@code Name}.
 * </li>
 * <li>
 * {@code Description}: (Optional) A human readable description of the metric.
 * </li>
 * <li>
 * {@code Type}: (Required) The type of the metric. See {@link MetricType}.
 * </li>
 * <li>
 * {@code Unit}: (Optional) The unit of the metric.
 * The unit may be any unit specified as a String or one specified in {@link MetricUnits}.
 * </li>
 * <li>
 * {@code Tags}: (Optional) The tags (represented by key/value pairs) of the metric which is augmented by global tags (if available).
 * Global tags can be set by passing the list of tags in an environment variable {@code MP_METRICS_TAGS}.
 * For example, the following can be used to set the global tags:
 * <pre><code>
 *      export MP_METRICS_TAGS=app=shop,tier=integration
 * </code></pre>
 * </li>
 * </ul>
 *
 * @author hrupp, Raymond Lam
 */
public interface Metadata {


    /**
     * Returns the metric name.
     *
     * @return the metric name.
     */
    String getName();

    /**
     * Returns the display name if set, otherwise this method returns the metric name.
     *
     * @return the display name
     */
    Optional<String> getDisplayName();


    /**
     * Returns the description of the metric.
     *
     * @return the description
     */
    Optional<String> getDescription();


    /**
     * Returns the String representation of the {@link MetricType}.
     *
     * @return the MetricType as a String
     * @see MetricType
     */
    String getType();

    /**
     * Returns the {@link MetricType} of the metric
     *
     * @return the {@link MetricType}
     */
    MetricType getTypeRaw();


    Optional<String> getUnit();

    boolean isReusable();

    /**
     * Gets the list of tags as a single String in the format 'key="value",key2="value2",...'
     *
     * @return a String containing the tags
     */
    String getTagsAsString();

    /**
     * Returns the underlying HashMap containing the tags.
     *
     * @return a hashmap of tags
     */
    Map<String, String> getTags();

}
