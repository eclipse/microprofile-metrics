/*
 * ********************************************************************
 *  Copyright (c) 2017, 2019 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.metrics.test;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Timed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricAppBean2 {

    @Inject
    private MetricRegistry registry;

    @Counted(name = "countMe2", absolute = true)
    public void countMeA() {

    }

    @Counted(name = "countMe2", absolute = true)
    public void countMeB() {

    }

    @Metered(name = "meterMe2")
    public void meterMeA() {

    }

    @Metered(name = "meterMe2")
    public void meterMeB() {

    }

    @Timed(absolute = true, name = "timeMe2")
    public void timeMeA() {

    }
    @Timed(absolute = true, name = "timeMe2")
    public void timeMeB() {

    }

    @SimplyTimed(absolute = true, name = "simplyTimeMe2")
    public void simplyTimeMeA() {

    }
    @SimplyTimed(absolute = true, name = "simplyTimeMe2")
    public void simplyTimeMeB() {

    }

}
