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
 *      Marking as Abstract + Adding Metadata + Adding Registry Type
 */
package org.eclipse.microprofile.metrics;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public abstract class MetricRegistry {
    
    public enum Type {
        APPLICATION("application"), BASE("base"), VENDOR("vendor");
    
        private String name;
        
        private Type(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    /**
     * Concatenates elements to form a dotted name, eliding any null values or empty strings.
     *
     * @param name     the first element of the name
     * @param names    the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    public static String name(String name, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, name);
        if (names != null) {
            for (String s : names) {
                append(builder, s);
            }
        }
        return builder.toString();
    }

    private static void append(StringBuilder builder, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
    }
    
    /**
     * Concatenates a class name and elements to form a dotted name, eliding any null values or
     * empty strings.
     *
     * @param klass    the first element of the name
     * @param names    the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    public static String name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    /**
     * Given a {@link Metric}, registers it under the given name.
     *
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the metric
     * @return {@code metric}
     * @throws IllegalArgumentException if the name is already registered
     */
    public abstract <T extends Metric> T register(String name, T metric) throws IllegalArgumentException;
    
    public abstract <T extends Metric> T register(String name, T metric, Metadata metadataEntry) throws IllegalArgumentException;


    /**
     * Return the {@link Counter} registered under this name; or create and register 
     * a new {@link Counter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Counter}
     */
    public abstract Counter counter(String name);
    
    public abstract Counter counter(Metadata metadata);



    /**
     * Return the {@link Histogram} registered under this name; or create and register 
     * a new {@link Histogram} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Histogram}
     */
    public abstract Histogram histogram(String name);
    public abstract Histogram histogram(Metadata metadata);


    /**
     * Return the {@link Meter} registered under this name; or create and register
     * a new {@link Meter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Meter}
     */
    public abstract Meter meter(String name);
    public abstract Meter meter(Metadata metadata);


    /**
     * Return the {@link Timer} registered under this name; or create and register
     * a new {@link Timer} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Timer}
     */
    public abstract Timer timer(String name);
    public abstract Timer timer(Metadata metadata);
 


    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return whether or not the metric was removed
     */
    public abstract boolean remove(String name);

    /**
     * Removes all metrics which match the given filter.
     *
     * @param filter a filter
     */
    public abstract void removeMatching(MetricFilter filter);


    /**
     * Returns a set of the names of all the metrics in the registry.
     *
     * @return the names of all the metrics
     */
    public abstract SortedSet<String> getNames();

    /**
     * Returns a map of all the gauges in the registry and their names.
     *
     * @return all the gauges in the registry
     */
    public abstract SortedMap<String, Gauge> getGauges();

    /**
     * Returns a map of all the gauges in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the gauges in the registry
     */
    public abstract SortedMap<String, Gauge> getGauges(MetricFilter filter);

    /**
     * Returns a map of all the counters in the registry and their names.
     *
     * @return all the counters in the registry
     */
    public abstract SortedMap<String, Counter> getCounters();

    /**
     * Returns a map of all the counters in the registry and their names which match the given
     * filter.
     *
     * @param filter    the metric filter to match
     * @return all the counters in the registry
     */
    public abstract SortedMap<String, Counter> getCounters(MetricFilter filter);

    /**
     * Returns a map of all the histograms in the registry and their names.
     *
     * @return all the histograms in the registry
     */
    public abstract SortedMap<String, Histogram> getHistograms();

    /**
     * Returns a map of all the histograms in the registry and their names which match the given
     * filter.
     *
     * @param filter    the metric filter to match
     * @return all the histograms in the registry
     */
    public abstract SortedMap<String, Histogram> getHistograms(MetricFilter filter);

    /**
     * Returns a map of all the meters in the registry and their names.
     *
     * @return all the meters in the registry
     */
    public abstract SortedMap<String, Meter> getMeters();

    /**
     * Returns a map of all the meters in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the meters in the registry
     */
    public abstract SortedMap<String, Meter> getMeters(MetricFilter filter);

    /**
     * Returns a map of all the timers in the registry and their names.
     *
     * @return all the timers in the registry
     */
    public abstract SortedMap<String, Timer> getTimers();

    /**
     * Returns a map of all the timers in the registry and their names which match the given filter.
     *
     * @param filter    the metric filter to match
     * @return all the timers in the registry
     */
    public abstract SortedMap<String, Timer> getTimers(MetricFilter filter);


    public abstract Map<String, Metric> getMetrics();

    // MP_METADATA
    public abstract Map<String, Metadata> getMetadata();
    
    public abstract Metadata getMetadata(String name);

}
