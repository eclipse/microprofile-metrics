/*
 **********************************************************************
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Default implementation of {@link org.eclipse.microprofile.metrics.GlobalTagsProvider} SPI.
 * <p>
 * This class is intentionally package private, and NOT discoverable by the
 * {@code ServiceLoader}, as it is intended to be used only by the exiting
 * {@link org.eclipse.microprofile.metrics.MetricID#MetricID(String, Tag...)
 * MetricID constructor}, in order to preserve backwards compatibility.
 * </p>
 * <p>
 * It is recommended that the MP Metrics implementations provide their own
 * implementation of this behavior.
 * </p>
 */
class DefaultGlobalTagsProvider implements GlobalTagsProvider {
    private static final String GLOBAL_TAGS_VARIABLE = "mp.metrics.tags";

    private static final String APPLICATION_NAME_VARIABLE = "mp.metrics.appName";
    private static final String APPLICATION_NAME_TAG = "_app";

    private static final String GLOBAL_TAG_MALFORMED_EXCEPTION = "Malformed list of Global Tags. Tag names "
                                                                + "must match the following regex [a-zA-Z_][a-zA-Z0-9_]*."
                                                                + " Global Tag values must not be empty."
                                                                + " Global Tag values MUST escape equal signs `=` and commas `,`"
                                                                + " with a backslash `\\` ";

    private volatile Set<Tag> tags = null;

    @Override
    public Set<? extends Tag> getGlobalTags() {
        if (tags == null) {
            synchronized (this) {
                if (tags == null) {
                    tags = new HashSet<>();

                    // add tags from providers discovered by the ServiceLoader first
                    ServiceLoader.load(GlobalTagsProvider.class)
                        .forEach(globalTagsProvider -> tags.addAll(globalTagsProvider.getGlobalTags()));

                    // add tags from configuration (configuration tags can override ServiceLoader-based tags)
                    try {
                        Config config = ConfigProvider.getConfig();
                        Optional<String> globalTags = config.getOptionalValue(GLOBAL_TAGS_VARIABLE, String.class);
                        globalTags.ifPresent(this::parseGlobalTags);

                        // for application servers with multiple applications deployed, distinguish metrics
                        // from different applications by adding the "_app" tag
                        Optional<String> applicationName = config.getOptionalValue(APPLICATION_NAME_VARIABLE, String.class);
                        applicationName.ifPresent(appName -> {
                            if (!appName.isEmpty()) {
                                tags.add(new Tag(APPLICATION_NAME_TAG, appName));
                            }
                        });
                    }
                    catch(NoClassDefFoundError | IllegalStateException | ExceptionInInitializerError ignore) {
                        // MP Config is probably not available, so just go on
                    }
                }
            }
        }

        return tags;
    }

    /**
     * Parses the global tags retrieved from configuration property {@code mp.metrics.tags}.
     *
     * @param globalTags the string of global tags retrieved from {@code mp.metrics.tags}
     *
     * @throws IllegalArgumentException if the global tags list does not adhere to
     *         the appropriate format.
     */
    private void parseGlobalTags(String globalTags) throws IllegalArgumentException {
        if (globalTags == null || globalTags.length() == 0) {
            return;
        }
        String[] kvPairs = globalTags.split("(?<!\\\\),");
        for (String kvString : kvPairs) {

            if (kvString.length() == 0) {
                throw new IllegalArgumentException(GLOBAL_TAG_MALFORMED_EXCEPTION);
            }

            String[] keyValueSplit = kvString.split("(?<!\\\\)=");

            if (keyValueSplit.length != 2 || keyValueSplit[0].length() == 0 || keyValueSplit[1].length() == 0) {
                throw new IllegalArgumentException(GLOBAL_TAG_MALFORMED_EXCEPTION);
            }

            String key = keyValueSplit[0];
            String value = keyValueSplit[1];

            if (!key.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                throw new IllegalArgumentException("Invalid Tag name. Tag names must match the following regex "
                                                   + "[a-zA-Z_][a-zA-Z0-9_]*");
            }
            value = value.replace("\\,", ",");
            value = value.replace("\\=", "=");
            tags.add(new Tag(key, value));
        }
    }
}
