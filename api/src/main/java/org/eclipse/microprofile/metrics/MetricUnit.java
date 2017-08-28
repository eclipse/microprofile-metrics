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
  NONE ("none", Family.NONE),

  /** A single Bit. Not defined by SI, but by IEC 60027 */
  BIT("bit", Family.BIT),
  /** 1000 {@link #BIT} */
  KILOBIT("kilobit", Family.BIT),
  /** 1000 {@link #KIBIBIT} */
  MEGABIT("megabit", Family.BIT),
  /** 1000 {@link #MEGABIT} */
  GIGABIT("gigabit", Family.BIT),
  /** 1024 {@link #BIT} */
  KIBIBIT("kibibit", Family.BIT),
  /** 1024 {@link #KIBIBIT}  */
  MEBIBIT("mebibit", Family.BIT),
  /** 1024 {@link #MEBIBIT} */
  GIBIBIT("gibibit", Family.BIT), /* 1024 mebibit */

  /** 8 {@link #BIT} */
  BYTE ("byte", Family.BYTE),
  /** 1024 {@link #BYTE} */
  KILOBYTE("kbyte", Family.BYTE), // 1024 bytes
  /** 1024 {@link #KILOBYTE} */
  MEGABYTE("mbyte", Family.BYTE), // 1024 kilo bytes
  /** 1024 {@link #MEGABYTE} */
  GIGABYTE("gbyte", Family.BYTE),

  NANOSECOND("ns", Family.TIME),
  MICROSECOND("us", Family.TIME),
  MILLISECOND("ms", Family.TIME),
  SECOND("s", Family.TIME),
  MINUTE("m", Family.TIME),
  HOUR("h", Family.TIME),
  DAY("d", Family.TIME),

  PERCENT("%", Family.RATE)

  ;


  private final String name;
  private final Family family;

  MetricUnit(String name, Family family) {
    this.name = name;
    this.family = family;
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
    throw new IllegalArgumentException(in + " is not a valid MetricUnit");
  }


  private enum Family {
    BIT,
    BYTE,
    TIME,
    RATE,
    NONE
  }

}