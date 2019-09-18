/*
 **********************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.annotation;

import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a shared metadata definition that can be referenced by regular metric annotations through their `metadata` parameter
 * that needs to match the value of {@link NamedMetadata#name()}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NamedMetadatas.class)
public @interface NamedMetadata {

    /**
     * Name of the shared metadata definition. Must be unique across a deployment.
     */
    String name();

    /**
     * Name that will be assigned to metrics that get created out of this metadata definition.
     */
    String metricName();

    /**
     * Display name that will be assigned to metrics that get created out of this metadata definition.
     */
    String displayName() default "";

    /**
     * Description that will be assigned to metrics that get created out of this metadata definition.
     */
    String description() default "";

    /**
     * Unit that will be assigned to metrics that get created out of this metadata definition.
     */
    String unit() default MetricUnits.NONE;

    /**
     * Type of the metrics that can be created out of this metadata definition.
     */
    MetricType type();

    /**
     * Whether the metrics created out of this metadata definition will be reusable. Must be false
     * in case that the {@link NamedMetadata#type()} is {@link MetricType#GAUGE}.
     */
    boolean reusable() default false;

}
