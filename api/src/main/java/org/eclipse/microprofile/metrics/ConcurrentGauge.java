/**
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
package org.eclipse.microprofile.metrics;

/**
 * A concurrent gauge is a gauge that measures parallel invocations of a method.
 *
 * In MicroProfile-Metrics before 2.0 this was done via @Counted(monotonic=false),
 * which led to confusion
 *
 * @author hrupp
 * @since 2.0
 *
 */
public interface ConcurrentGauge extends Metric {

  long getCount();
  long getMax();
  long getMin();

  void inc();
  void dec();

}
