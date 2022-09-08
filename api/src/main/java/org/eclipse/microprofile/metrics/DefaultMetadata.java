/*
 **********************************************************************
 * Copyright (c) 2018, 2022 Contributors to the Eclipse Foundation
 *               2018 Red Hat, Inc. and/or its affiliates
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

import java.util.Objects;
import java.util.Optional;

/**
 * The default implementation of {@link Metadata}
 */
public class DefaultMetadata implements Metadata {

    /**
     * Name of the metric.
     * <p>
     * A required field which holds the name of the metric object.
     * </p>
     */
    private final String name;

    /**
     * A human readable description.
     * <p>
     * An optional field which holds the description of the metric object.
     * </p>
     */
    private final String description;

    /**
     * Unit of the metric.
     * <p>
     * An optional field which holds the Unit of the metric object.
     * </p>
     */
    private final String unit;

    protected DefaultMetadata(String name, String description, String unit) {
        this.name = name;
        this.description = description;
        this.unit = unit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description().orElse("");
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public String getUnit() {
        return unit().orElse(MetricUnits.NONE);
    }

    @Override
    public Optional<String> unit() {
        return Optional.ofNullable(unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Metadata)) {
            return false;
        }
        Metadata that = (Metadata) o;

        // Use getters to compare the effective values
        return Objects.equals(name, that.getName()) &&
                Objects.equals(this.getDescription(), that.getDescription()) &&
                Objects.equals(this.getUnit(), that.getUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, unit);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultMetadata{");
        sb.append("name='").append(name).append('\'');
        sb.append(", unit='").append(unit).append('\'');
        if (description != null) {
            sb.append(", description='").append(description).append('\'');
        } else {
            sb.append(", description=null");
        }
        sb.append('}');
        return sb.toString();
    }
}
