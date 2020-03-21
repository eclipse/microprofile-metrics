/*
 **********************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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

/**
 * The {@link Metadata} builder.
 */
public class MetadataBuilder {

    private String name;

    private String displayName;

    private String description;

    private MetricType type;

    private String unit;

    MetadataBuilder(Metadata metadata) {
        this.name = metadata.getName();
        this.type = metadata.getTypeRaw();
        metadata.getDescriptionOptional().ifPresent(this::withDescription);
        metadata.getUnitOptional().ifPresent(this::withUnit);
        metadata.getDisplayNameOptional().ifPresent(this::withDisplayName);
    }

    public MetadataBuilder() {

    }

    /**
     * Sets the name. Does not accept null.
     *
     * @param name the name
     * @return the builder instance
     * @throws NullPointerException when name is null
     */
    public MetadataBuilder withName(String name) {
        this.name = Objects.requireNonNull(name, "name is required");
        return this;
    }

    /**
     * Sets the displayName. Does not accept null.
     *
     * @param displayName the displayName
     * @return the builder instance
     * @throws NullPointerException when displayName is null
     */
    public MetadataBuilder withDisplayName(String displayName) {
        this.displayName = "".equals(displayName) ? null : displayName;
        return this;
    }

    /**
     * Sets the description. Does not accept null.
     *
     * @param description the name
     * @return the builder instance
     * @throws NullPointerException when description is null
     */
    public MetadataBuilder withDescription(String description) {
        this.description = "".equals(description) ? null : description;
        return this;
    }

    /**
     * Sets the type. Does not accept null.
     *
     * @param type the type
     * @return the builder instance
     * @throws NullPointerException when type is null
     */
    public MetadataBuilder withType(MetricType type) {
        this.type = MetricType.INVALID == type ? null : type;
        return this;
    }

    /**
     * Sets the unit. Does not accept null.
     *
     * @param unit the unit
     * @return the builder instance
     * @throws NullPointerException when unit is null
     */
    public MetadataBuilder withUnit(String unit) {
        this.unit = MetricUnits.NONE.equals(unit) ? null : unit;
        return this;
    }


    /**
     * @return An object implementing {@link Metadata} from the provided properties
     * @throws IllegalStateException when either name is null
     */
    public Metadata build() {
        if (Objects.isNull(name)) {
            throw new IllegalStateException("Name is required");
        }

        return new DefaultMetadata(name, displayName, description, type, unit);
    }
}
