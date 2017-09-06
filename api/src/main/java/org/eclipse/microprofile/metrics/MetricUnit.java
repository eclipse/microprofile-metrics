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

/**
 * Units for the metrics.
 *
 * @author hrupp
 */
public final class MetricUnit {
  /** Dummy to say that this has no unit */
  public static final String NONE = "none";

  /** A single Bit. Not defined by SI, but by IEC 60027 */
  public static final String BITS = "bits";
  /** 1000 {@link #BITS} */
  public static final String KILOBITS = "kilobits";
  /** 1000 {@link #KIBIBITS} */
  public static final String MEGABITS = "megabits";
  /** 1000 {@link #MEGABITS} */
  public static final String GIGABITS = "gigabits";
  /** 1024 {@link #BITS} */
  public static final String KIBIBITS = "kibibits";
  /** 1024 {@link #KIBIBITS}  */
  public static final String MEBIBITS = "mebibits";
  /** 1024 {@link #MEBIBITS} */
  public static final String GIBIBITS = "gibibits";

  /** 8 {@link #BITS} */
  public static final String BYTES = "bytes";
  /** 1024 {@link #BYTES} */
  public static final String KILOBYTES = "kilobytes";
  /** 1024 {@link #KILOBYTES} */
  public static final String MEGABYTES = "megabytes";
  /** 1024 {@link #MEGABYTES} */
  public static final String GIGABYTES = "gigabytes";

  public static final String NANOSECONDS = "nanoseconds";
  public static final String MICROSECONDS = "microseconds";
  public static final String MILLISECONDS = "milliseconds";
  public static final String SECONDS = "seconds";
  public static final String MINUTES = "minutes";
  public static final String HOURS = "hours";
  public static final String DAYS = "days";

  public static final String PERCENT = "%";
  public static final String PER_SECOND = "per_second";
  
  
  private MetricUnit() {}
}