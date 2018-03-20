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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

class DefaultMetadata implements Metadata{

    /**
     * Name of the metric.
     * <p>
     * A required field which holds the name of the metric object.
     * </p>
     */
    private String name;

    /**
     * Display name of the metric. If not set, the name is taken.
     * <p>
     * An optional field which holds the display (Friendly) name of the metric object.
     * By default it is set to the name of the metric object.
     * </p>
     */
    private String displayName;

    /**
     * A human readable description.
     * <p>
     * An optional field which holds the description of the metric object.
     * </p>
     */
    private String description;

    /**
     * Type of the metric.
     * <p>
     * A required field which holds the type of the metric object.
     * </p>
     */
    private MetricType type = MetricType.INVALID;
    /**
     * Unit of the metric.
     * <p>
     * An optional field which holds the Unit of the metric object.
     * </p>
     */
    private String unit = MetricUnits.NONE;

    /**
     * Can this metric name (in a scope) be used multiple times?
     * <p>
     * Setting this is optional. The default is <tt>false</tt>, which
     * prevents reusing.
     * </p>
     * Note that this only has an effect if the <tt>name</tt> is explicitly given or
     * <tt>absolute</tt> is set to true and two methods that are marked as metric have
     * the same name.
     * <p>
     * If the name is automatically determined, then this flag has no effect as
     * all metric names are different anyway
     */
    private boolean reusable = false;

    /**
     * Tags of the metric. Augmented by global tags.
     * <p>
     * An optional field which holds the tags of the metric object which can be
     * augmented by global tags.
     * </p>
     */
    private Map<String, String> tags = new HashMap<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public String getType() {
        return Optional.ofNullable(type).orElse(MetricType.INVALID).toString();
    }

    @Override
    public MetricType getTypeRaw() {
        return Optional.ofNullable(type).orElse(MetricType.INVALID);
    }

    @Override
    public Optional<String> getUnit() {
        return Optional.ofNullable(unit);
    }

    @Override
    public boolean isReusable() {
        return reusable;
    }

    @Override
    public String getTagsAsString() {
        StringBuilder result = new StringBuilder();

        Iterator<Map.Entry<String, String>> iterator = this.tags.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> pair = iterator.next();
            result.append(pair.getKey()).append("=\"").append(pair.getValue()).append("\"");
            if (iterator.hasNext()) {
                result.append(",");
            }

        }

        return result.toString();
    }

    @Override
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }
}
