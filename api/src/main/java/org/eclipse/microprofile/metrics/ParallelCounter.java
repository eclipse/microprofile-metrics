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
package org.eclipse.microprofile.metrics;

/**
 * An incrementing and decrementing counter metric.
 * Unlike a Hit Counter, it has a high water mark, that can be queried
 * and reset.
 */
public interface ParallelCounter extends Metric, Counting {

    /**
     * Increment the counter by one.
     */
    void inc();

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void inc(long n);

    /**
     * Decrement the counter by one.
     */
    void dec();

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be decreased
     */
    void dec(long n);

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    @Override
    long getCount();

    /**
     * Return the high water mark for the counter
     * @return highest value since the last reset
     */
    long getRecentPeak();

    /**
     * Reset the high water mark
     */
    void resetRecentPeak();


}
