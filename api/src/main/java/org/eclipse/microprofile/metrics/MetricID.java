/*
 **********************************************************************
 * Copyright (c) 2018, 2019 Contributors to the Eclipse Foundation
 *               2018, 2019 IBM Corporation and others
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
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
 * {@code Tags}: (Optional) The tags (represented by {@link Tag} objects)
 * of the metric. The tag name
 * must match the regex `[a-zA-Z_][a-zA-Z0-9_]*` (Ascii alphabet, numbers and
 * underscore). The tag value may contain any UTF-8 encoded character.
 * </li>
 * </ul>
 */
public class MetricID implements Comparable<MetricID> {

    /**
     * Name of the metric.
     * <p>
     * A required field which holds the name of the metric object.
     * </p>
     */
    private final String name;

    /**
     * Tags of the metric.
     */
    private final Map<String, String> tags = new TreeMap<String, String>();

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
     * Constructs a MetricID with the given metric name and {@link Tag}s.
     * If global tags are available then they will be appended to this MetricID
     *
     * @param name the name of the metric
     * @param tags the tags associated with this metric
     */
    public MetricID(String name, Tag... tags) {
        this.name = name;
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
     * Returns the underlying map containing the tags.
     *
     * @return a {@link Map} of tags
     */
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
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
     * Gets the list of tags as a list of {@link Tag} objects
     *
     * @return a a list of Tag objects
     */
    public List<Tag> getTagsAsList() {
        List<Tag> list = new ArrayList<>();
        this.tags.forEach((key, value) -> list.add(new Tag(key, value)));
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets the list of tags as an array of {@link Tag} objects.
     *
     * @return An array of tags
     */
    public Tag[] getTagsAsArray() {
        Tag[] result = new Tag[tags.size()];
        int i = 0;
        for (Entry<String, String> entry : tags.entrySet()) {
            result[i] = new Tag(entry.getKey(), entry.getValue());
            i++;
        }
        return result;
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

    @Override
    public String toString() {
        return "MetricID{" +
            "name='" + name + '\'' +
            ", tags=[" + getTagsAsString() +
            "]}";
    }

    /**
     * Add multiple {@link Tag} objects
     * This method will call {@link #addTag(Tag)} on each tag.
     * If a key already exists then it will be overwritten.
     *
     * @param tagArray an array of {@link Tag} objects
     */
    private void addTags(Tag[] tagArray) {
        if (tagArray == null || tagArray.length == 0) {
            return;
        }
        Stream.of(tagArray).forEach(this::addTag);
    }

    /**
     * Adds a singular {@link Tag} object into the MetricID.
     * If the tag key/name already exists then it will be overwritten with
     * the new value.
     *
     * @param tag the {@link Tag} object
     */
    private void addTag(Tag tag) {
        if (tag == null || tag.getTagName() == null || tag.getTagValue() == null) {
            return;
        }
        tags.put(tag.getTagName(), tag.getTagValue());
    }

    /**
     * Compares two MetricID objects through the following steps:
     * <br>
     * <ol>
     * <li>
     * Compares the names of the two MetricIDs lexicographically.
     * </li>
     * <li>
     * If the names are equal: Compare the number of tags.
     * </li>
     * <li>
     * If the tag lengths are equal: Compare the Tags (sorted by the Tag's key value)
     * <ul>
     * <li>
     * a) Compare the Tag names/keys lexicographically
     * </li>
     * <li>
     * b) If keys are equal, compare the Tag values lexicographically
     * </li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param other the other MetricID
     */
    @Override
    public int compareTo(MetricID other) {
        int compareVal = this.name.compareTo(other.getName());
        if (compareVal == 0) {
            compareVal = this.tags.size() - other.getTags().size();
            if (compareVal == 0) {
                Iterator<Entry<String, String>> thisIterator = tags.entrySet().iterator();
                Iterator<Entry<String, String>> otherIterator = other.getTags().entrySet().iterator();
                while (thisIterator.hasNext() && otherIterator.hasNext()) {
                    Entry<String, String> thisEntry = thisIterator.next();
                    Entry<String, String> otherEntry = otherIterator.next();
                    compareVal = thisEntry.getKey().compareTo(otherEntry.getKey());
                    if (compareVal != 0) {
                        return compareVal;
                    }
                    else {
                        compareVal = thisEntry.getValue().compareTo(otherEntry.getValue());
                        if (compareVal != 0) {
                            return compareVal;
                        }
                    }
                }
            }
        }
        return compareVal;
    }
}
