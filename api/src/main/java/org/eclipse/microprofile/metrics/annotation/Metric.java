/**********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *               2012 Ryan W Tenney (ryan@10e.us)
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.eclipse.microprofile.metrics.MetricUnit;

/**
 * An annotation requesting that a metric be injected or registered.
 *
 * Given a field like this:
 * <pre><code>
 *     {@literal @}Metric
 *     public Histogram histogram;
 * </code></pre>
 * A meter of the field's type will be created and injected into managed objects.
 * It will be up to the user to interact with the metric. This annotation
 * can be used on fields of type Meter, Timer, Counter, and Histogram.
 *
 * This may also be used to register a metric, which is useful for creating a histogram with
 * a custom Reservoir.
 * <pre><code>
 *     {@literal @}Metric
 *     public Histogram uniformHistogram = new Histogram(new UniformReservoir());
 * </code></pre>
 *
 * 
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface Metric {

    /**
     * @return The metric's name.
     */
    @Nonbinding
    String name() default "";

    /**
     * @return The metric's tags.
     */
    @Nonbinding
    String[] tags() default {};

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false},
     * use the given name relative to the annotated class.
     */
    @Nonbinding
    boolean absolute() default false;

    /**
     * 
     * @return display name of the timer from Metadata
     */
    @Nonbinding
    String displayName() default "";
    
    /**
     * 
     * @return description of the timer from Metadata
     */
    @Nonbinding
    String description() default "";
    
   /**
    * @return unit of the metrics from Metadata
    *
    */
    @Nonbinding
    String unit() default MetricUnit.NONE;

}
