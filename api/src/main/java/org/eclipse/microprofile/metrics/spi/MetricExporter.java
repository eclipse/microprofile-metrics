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
   * @return
   */
  String getMediaType();

  /**
   * The http method this exporter reacts to
   * @return
   */
  HttpMethod getMethod();

  /**
   * Export one single metric of a scope
   * @param scope The scope
   * @param metricName The name of the metric
   * @return An expression in the right content type. See {@link #getContentType()}.
   */
  StringBuffer exportOneMetric(MetricRegistry.Type scope, String metricName);

  /**
   * Export all metrics of a scope
   * @param scope The scope
   * @return An expression containing all metrics, encoded in the right content type. See {@link #getContentType()}.
   */
  StringBuffer exportOneScope(MetricRegistry.Type scope);

  /**
   * Export all metrics
   * @return An expression containing all metrics, encoded in the right content type. See {@link #getContentType()}.
   */
  StringBuffer exportAllScopes();



  enum HttpMethod {
    GET,
    OPTIONS
  }
}
