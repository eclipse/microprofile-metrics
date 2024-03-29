/*
 **********************************************************************
 * Copyright (c) 2018, 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.tck.util;

import org.junit.Assert;

public class TestUtils {

    public static final double TOLERANCE = 0.15;

    // private constructor
    private TestUtils() {
    }

    /**
     * Assert equals with a tolerance factor of {@link #TOLERANCE} times the expected value
     *
     * @param expected
     *            expected value
     * @param actual
     *            the actual value
     */
    public static void assertEqualsWithTolerance(double expected, double actual) {
        Assert.assertEquals(expected, actual, expected * TOLERANCE);
    }
}
