/*
 * Copyright (C) 2010-2013 Coda Hale, Yammer.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   2013-04-20 - Coda Hale
 *      Initially authored in dropwizard/metrics SHA:afcf7fd6a12a0f133641
 *   2017-08-17 - Raymond Lam / Ouyang Zhou / IBM Corp
 *      Added Metadata fields
 */
package org.eclipse.microprofile.metrics.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnit;

/**
 * An annotation for marking a method of an annotated object as timed.
 * Given a method like this:
 * <pre><code>
 *     {@literal @}Timed(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * A timer for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the method's execution will be timed.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Timed {

    /**
     * @return The timer's name.
     */
    String name() default "";

    /**
     * @return The timer's tags.
     *
     * Will be used as metadata tags
     */
    String[] tags() default {};

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class. When annotating a class, this must be {@code false}.
     */
    boolean absolute() default false;

    /**
     *
     * @return display name of the timer from Metadata
     */
    String displayName() default "";

    /**
     * @return mbean of the timer from Metadata
     */
    String mbean() default "";

    /**
     *
     * @return description of the timer from Metadata
     */
    String description() default "";

    /**
     *
     * @return type of the metrics from Metadata, which is a timer for timed
     */
    MetricType type() default MetricType.TIMER;


    /**
     * @return unit of the metrics from Metadata
     *
     */
    MetricUnit unit() default MetricUnit.SECOND;

    /**
     *
     * @return specified in Metadata for whether the metric can have multiple objects and need special treatment
     */
    boolean multi() default false;

}