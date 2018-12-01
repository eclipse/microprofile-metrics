/*
 **********************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *               2018 IBM Corporation and others
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * A unique identifier for {@link Metric} and {@link Metadata} that are registered
 * in the {@link MetricRegistry}
 *
 * The MetricID contains:
 * <ul>
 * <li>
 * {@code Name}: (Required) The name of the metric.
 * </li>
 * <li>
 * {@code Tags}: (Optional) The tags (represented by key/value pairs) of the metric which is augmented by global tags (if available).
 * Global tags can be set by passing the list of tags in an environment variable {@code MP_METRICS_TAGS}.
 * For example, the following can be used to set the global tags:
 *
 * <pre>
 * <code>
 *      export MP_METRICS_TAGS=app=shop,tier=integration
 * </code>
 * </pre>
 *
 * </li>
 * </ul>
 */
public class MetricID {

    public static final String GLOBAL_TAGS_VARIABLE = "MP_METRICS_TAGS";

    /**
     * Name of the metric.
     * <p>
     * A required field which holds the name of the metric object.
     * </p>
     */
    public final String name;

    /**
     * Tags of the metric. Augmented by global tags.
     * <p>
     * An optional field which holds the tags of the metric object which can be
     * augmented by global tags.
     * </p>
     */
    private final Map<String, String> tags = new HashMap<String, String>();

    /**
     * Constructs a MetricID with the given metric name and no tags.
     * If global tags are available then they will be appended to this MetricID.
     *
     * @param name the name of the metric
     */
    public MetricID(String name) {
        this(name, null);
    }

    /**
     * Constructs a MetricID with the given metric name and tags.
     * If global tags are available then they will be appended to this MetricID
     *
     * @param name the name of the metric
     * @param tags the tags associated with this metric
     */
    public MetricID(String name, String tags) {
        this.name = name;
        String globalTagsFromEnv = System.getenv(GLOBAL_TAGS_VARIABLE);
        addTags(globalTagsFromEnv);
        addTags(tags);
    }

    /**
     * Returns the Metric name associated with this MetricID
     *
     * @return the Metric name associated with this MetricID
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of tags as a single String in the format 'key="value",key2="value2",...'
     *
     * @return a String containing the tags
     */
    public String getTagsAsString() {
        return this.tags.entrySet().stream().map(e -> e.getKey() + "=\"" + e.getValue() + "\"").collect(Collectors.joining(","));
    }

    /**
     * Returns the underlying HashMap containing the tags.
     *
     * @return a {@link Map} of tags
     */
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetricID)) {
            return false;
        }
        MetricID that = (MetricID) o;
        return Objects.equals(this.name, that.getName()) && Objects.equals(this.tags, that.getTags());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, tags);
    }

    /**
     * Add multiple tags delimited by commas.
     * The format must be in the form 'key1=value1, key2=value2'.
     * This method will call {@link #addTag(String)} on each tag.
     *
     * @param tagsString a string containing multiple tags
     */
    public void addTags(String tagsString) {
        if (tagsString == null || tagsString.isEmpty()) {
            return;
        }
        String[] singleTags = tagsString.split(",");
        Stream.of(singleTags).map(String::trim).forEach(this::addTag);
    }

    /**
     * Add one single tag with the format: 'key=value'. If the input is empty or does
     * not contain a '=' sign, the entry is ignored.
     *
     * @param kvString Input string
     */
    public void addTag(String kvString) {
        if (kvString == null || kvString.isEmpty() || !kvString.contains("=")) {
            return;
        }
        tags.put(kvString.substring(0, kvString.indexOf("=")), kvString.substring(kvString.indexOf("=") + 1));
    }

}
