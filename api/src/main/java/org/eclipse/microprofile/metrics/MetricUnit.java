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
 * 
 * Contributors:
 *   2017-05-05 - Heiko Rupp
 *      Initial contribution
 *   2017-08-22 - Raymond Lam
 *      Renaming MpMType to MetricType and MpMUnit to MetricUnit
 *   2017-08-26 - Werner Keil
 *      Marking as Interface
 */
package org.eclipse.microprofile.metrics;

/**
 * A Unit for the metrics.
 *
 * @author hrupp
 * @author raymondlam
 * @author keilw
 */
public interface MetricUnit {
    
    /**
     * Returns the name of this unit.
     * 
     * @return the name of the unit
     * @see #toString()
     */
    String getName();

    /**
     * <p>
     * Returns a string representation of this unit.
     * 
     * @return the string representation of this unit.
     */
    @Override
    public String toString();
}