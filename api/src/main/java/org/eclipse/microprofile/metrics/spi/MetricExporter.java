/*
 * ********************************************************************
 *  Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 *  See the NOTICES file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ********************************************************************
 *
 */
package org.eclipse.microprofile.metrics.spi;

import java.util.Map;
import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * The exporter interface. Metric exporters need to implement this interface.
 *
 * @author hrupp
 */
public interface MetricExporter {

  /**
   * Exporter priority. If two exporters have the same media type and http verb,
   * the one with the higher priority is used.
   * @return Priority of the exporter.
   */
  int getPriority();

  /**
   * Media type this exporter produces. E.g. text/html or application/json
   * @return The media type
   */
  String getMediaType();

  /**
   * The http method this exporter reacts to.
   * The default is GET.
   * @return the http method this exporter supports
   */
  default HttpMethod getMethod() {
    return HttpMethod.GET;
  }

  /**
   * Export one single metric of a scope
   * @param scope The scope
   * @param metricName The name of the metric
   * @return A String in the right content type. See {@link #getMediaType()}.
   */
  String exportOneMetric(MetricRegistry.Type scope, String metricName);

  /**
   * Export all metrics of a scope
   * @param scope The scope
   * @return A String containing all metrics, encoded in the right content type. See {@link #getMediaType()}.
   */
  String exportOneScope(MetricRegistry.Type scope);

  /**
   * Export all metrics
   * @return A String containing all metrics, encoded in the right content type. See {@link #getMediaType()}.
   */
  String exportAllScopes();

  /**
   * Used to pass the configured registries to the exporter
   * @param registryMap A map that contains all the registries (base, application, vendor)
   */
  void setRegistries(Map<MetricRegistry.Type,MetricRegistry> registryMap);


  enum HttpMethod {
    GET,
    OPTIONS
  }
}
