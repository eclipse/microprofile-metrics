/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.metrics;


import java.util.EnumSet;

/**
 * Units for the metrics.
 *
 * @author hrupp
 */
public enum MetricUnit {
  /** Dummy to say that this has no unit */
  NONE ("none", Family.NONE, 1),

  /** A single Bit. Not defined by SI, but by IEC 60027 */
  BIT("bit", Family.BIT, 1),
  /** 1000 {@link #BIT} */
  KILOBIT("kilobit", Family.BIT, 1_000),
  /** 1000 {@link #KIBIBIT} */
  MEGABIT("megabit", Family.BIT, 1_000_000),
  /** 1000 {@link #MEGABIT} */
  GIGABIT("gigabit", Family.BIT, 1_000_000_000),
  /** 1024 {@link #BIT} */
  KIBIBIT("kibibit", Family.BIT, 1_024),
  /** 1024 {@link #KIBIBIT}  */
  MEBIBIT("mebibit", Family.BIT, 1_024 * 1_024),
  /** 1024 {@link #MEBIBIT} */
  GIBIBIT("gibibit", Family.BIT, 1_024 * 1_024 * 1_024), /* 1024 mebibit */

  /** 8 {@link #BIT} */
  BYTE ("byte", Family.BYTE, 1),
  /** 1024 {@link #BYTE} */
  KILOBYTE("kbyte", Family.BYTE, 1_024), // 1024 bytes
  /** 1024 {@link #KILOBYTE} */
  MEGABYTE("mbyte", Family.BYTE, 1_024 * 1_024), // 1024 kilo bytes
  /** 1024 {@link #MEGABYTE} */
  GIGABYTE("gbyte", Family.BYTE, 1_024 * 1_024 * 1_024),

  NANOSECOND("ns", Family.TIME, 1d/1_000_000),
  MICROSECOND("us", Family.TIME, 1d/1_000_000),
  MILLISECOND("ms", Family.TIME, 1d/1000),
  SECOND("s", Family.TIME, 1),
  MINUTE("m", Family.TIME, 60),
  HOUR("h", Family.TIME, 3600),
  DAY("d", Family.TIME, 86400),

  PERCENT("%", Family.PERCENT, 1)

  ;


  private final String name;
  private final Family family;
  private final double factor;

  MetricUnit(String name, Family family, double factor) {
    this.name = name;
    this.family = family;
    this.factor = factor;
  }

  @Override
  public String toString() {
    return name;
  }

  public static MetricUnit from(String in) {
    EnumSet<MetricUnit> enumSet = EnumSet.allOf(MetricUnit.class);
    for (MetricUnit u : enumSet) {
      if (u.name.equals(in)) {
        return u;
      }
    }
    throw new IllegalArgumentException(in + " is not a valid MpUnit");
  }

  /**
   * Given a numeric input value and its unit, the method will scale it to
   * the the respective value of the base unit of the family.
   * E.g. a value of 10 and a unit of "ms" will result in 0.001 (seconds).
   * @param value The numeric value to convert
   * @param unitIn The input unit
   * @return Double value of the scaled input
   */
  public static double scaleToBase(Number value, MetricUnit unitIn) {
    if (value instanceof Integer) {
      return (Integer)value * unitIn.factor;
    } 
    else if (value instanceof Long) {
      return (Long) value * unitIn.factor;
    } 
    else if (value instanceof Double) {
      return (Double) value * unitIn.factor;
    }
    else {
        throw new IllegalStateException("Unknown Number type for " + value );
    }
  }

  /**
   * For a given unit in a family return its base unit.
   * E.g. for time family (seconds etc) return #SECOND
   * @param unitIn Unit to investigate
   * @return Base unit
   */
  public static MetricUnit getBaseUnit(MetricUnit unitIn) {
    EnumSet<MetricUnit> enumSet = EnumSet.allOf(MetricUnit.class);
    for (MetricUnit u : enumSet) {
      if (u.family.equals(unitIn.family) && u.factor == 1) {
        return u;
      }
    }
    throw new IllegalArgumentException(unitIn + " is not a valid MpUnit");
  }

  /**
   * Return the metric unit as string suitable for Prometheus.
   * As Prometheus requires the base unit, the result is the
   * name from the base unit of the family, in belongs to.
   * @param in Unit
   * @return String representation for the base unit of in
   */
  public static String getBaseUnitAsPrometheusString(MetricUnit in) {
    MetricUnit base = getBaseUnit(in);
    String out;
    switch (base.family) {
      case BIT:  out = "bits"; break;
      case BYTE: out = "bytes"; break;
      case TIME: out = "seconds"; break;
      case PERCENT: out = "percent"; break;
      default:
        out = "";
    }
    return out;
  }

  private enum Family {
    BIT,
    BYTE,
    TIME,
    PERCENT,
    NONE
  }

}