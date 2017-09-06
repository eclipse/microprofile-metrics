/**********************************************************************
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Bean holding the metadata of one single metric
 * @author hrupp, Raymond Lam
 */
public class Metadata {
    
     /**
     * Name of the metric.
     * <p>
     * A required field which holds the name of the metric object. Can be retrieved from
     * other reporters such as REST Handler, HTTP Reporter.
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
    private String unit = MetricUnit.NONE;
    
    /**
     * Tags of the metric. Augmented by global tags.
     * <p>
     * An optional field which holds the tags of the metric object which can be
     * augmented by global tags.
     * </p>
     */
    private HashMap<String, String> tags = new HashMap<String, String>();

    /**
     * Defines if the metric can have multiple objects and needs special
     * treatment or if it is a singleton.
     * <p/>
     */
    
    Metadata() {
        String globalTagsFromEnv = System.getenv("MP_METRICS_TAGS");

        // We use the parsing logic, but need to save them away, as the yaml
        // Config will overwrite them otherwise.
        addTags(globalTagsFromEnv);
    }

    /**
     * Constructs a Metadata object with default Units
     * 
     * @param name The name of the metric
     * @param type The type of the metric
     */
    public Metadata(String name, MetricType type) {
        // MP-Metrics, set default value for other fileds
        this();
        this.name = name;
        this.type = type;

        // Assign default units
        switch (type) {
        case TIMER:
            this.unit = MetricUnit.NANOSECONDS;
            break;
        case METERED:
            this.unit = MetricUnit.PER_SECOND;
            break;
        case HISTOGRAM:
        case GAUGE:
        case COUNTER:
        default:
            this.unit = MetricUnit.NONE;
            break;
        }
    }

    /**
     * Constructs a Metadata object
     * 
     * @param name The name of the metric
     * @param type The type of the metric
     * @param unit The units of the metric
     */
    public Metadata(String name, MetricType type, String unit) {
        this();
        this.name = name;
        this.type = type;
        this.unit = unit;
    }

    /**
     * Constructs a Metadata object
     * 
     * @param name The name of the metric
     * @param displayName The display (friendly) name of the metric
     * @param description The description of the metric
     * @param type The type of the metric
     * @param unit The units of the metric
     */
    public Metadata(String name, String displayName, String description, MetricType type, String unit) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.unit = unit;
    }

    /**
     * Constructs a Metadata object
     * 
     * @param name The name of the metric
     * @param displayName The display (friendly) name of the metric
     * @param description The description of the metric
     * @param type The type of the metric
     * @param unit The units of the metric
     * @param tags The tags of the metric
     */
    public Metadata(String name, String displayName, String description, MetricType type, String unit, String tags) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.unit = unit;
        addTags(tags);
    }

    public Metadata(Map<String, String> in) {
        this();
        this.name = (String) in.get("name");
        this.description = (String) in.get("description");
        this.displayName = (String) in.get("displayName");
        this.setType((String) in.get("type"));
        this.setUnit((String) in.get("unit"));
        if (in.keySet().contains("tags")) {
            String tagString = (String) in.get("tags");
            String[] tagList = tagString.split(",");
            for (String tag : tagList) {
                this.tags.put(tag.substring(0, tag.indexOf("=")), tag.substring(tag.indexOf("=")));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return name;
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type == null ? MetricType.INVALID.toString() : type.toString();
    }

    public MetricType getTypeRaw() {
        return type;
    }

    public void setType(String type) {
        this.type = MetricType.from(type);
    }

    public void setType(MetricType type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTagsAsString() {
        StringBuilder result = new StringBuilder();

        Iterator<Entry<String, String>> iterator = this.tags.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> pair = iterator.next();
            result.append(pair.getKey()).append("=\"").append(pair.getValue()).append("\"");
            if (iterator.hasNext()) {
                result.append(",");
            }

        }

        return result.toString();
    }

    public HashMap<String, String> getTags() {

        return this.tags;
    }

    /**
     * Add one single tag. Format is 'key=value'. If the input is empty or does
     * not contain a '=' sign, the entry is ignored.
     * 
     * @param kvString
     *            Input string
     */
    public void addTag(String kvString) {
        if (kvString == null || kvString.isEmpty() || !kvString.contains("=")) {
            return;
        }
        tags.put(kvString.substring(0, kvString.indexOf("=")), kvString.substring(kvString.indexOf("=") + 1));
    }

    public void addTags(String tagsString) {
        if (tagsString == null || tagsString.isEmpty()) {
            return;
        }

        String[] singleTags = tagsString.split(",");
        for (String singleTag : singleTags) {
            addTag(singleTag.trim());
        }
    }

    public void setTags(HashMap<String, String> tags) {
        this.tags = tags;
    }

    /**
     * public boolean equals(Object o) { //if (this == o) return true; //if (o
     * == null || getClass() != o.getClass()) return false;
     * 
     * Metadata that = (Metadata) o;
     * 
     * if (!name.equals(that.name)) return false; if (mbean != null ?
     * !mbean.equals(that.mbean) : that.mbean != null) return false; if
     * (!type.equals(that.type)) return false; return unit.equals(that.unit); }
     **/

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetadataEntry{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", unit='").append(unit).append('\'');
        sb.append(", tags='").append(tags).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
