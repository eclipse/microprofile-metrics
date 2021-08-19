/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.metrics.tck.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;

public final class MetricsUtil {

    private MetricsUtil() {
    }

    public static Set<String> absoluteMetricNames(Class<?> clazz, String... names) {
        Set<String> set = new HashSet<>(names.length);
        for (String name : names) {
            set.add(absoluteMetricName(clazz, name));
        }

        return set;
    }

    public static Set<String> absoluteMetricNames(Class<?> clazz, String prefix, String... names) {
        Set<String> set = new HashSet<>(names.length);
        for (String name : names) {
            set.add(absoluteMetricName(clazz, prefix, name));
        }

        return set;
    }

    public static Set<String> absoluteMetricNames(Class<?> clazz, String[] array, String... names) {
        Set<String> set = new HashSet<>(absoluteMetricNames(clazz, array));
        for (String name : names) {
            set.add(absoluteMetricName(clazz, name));
        }

        return set;
    }

    public static Set<String> absoluteMetricNames(Class<?> clazz, String prefix, String[] array, String... names) {
        Set<String> set = new HashSet<>(absoluteMetricNames(clazz, prefix, array));
        for (String name : names) {
            set.add(absoluteMetricName(clazz, prefix, name));
        }

        return set;
    }

    public static String absoluteMetricName(Class<?> clazz, String name) {
        return MetricRegistry.name(clazz, name);
    }

    public static String absoluteMetricName(Class<?> clazz, String metric, String name) {
        return MetricRegistry.name(clazz.getPackage().getName() + "." + metric, name);
    }

    public static Set<MetricID> createMetricIDs(Set<String> metricNames) {
        Set<MetricID> metricIDSet = new HashSet<MetricID>();
        for (String metricName : metricNames) {
            metricIDSet.add(new MetricID(metricName));
        }
        return metricIDSet;
    }
}
