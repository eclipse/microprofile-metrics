/*
 **********************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.tck.tags;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GaugeTagMethodBean {

    private long gaugeOne;
    private long gaugeTwo;

    @Gauge(name = "gaugeMethod", unit = MetricUnits.NONE, tags = {"number=one"})
    public long getGaugeOne() {
        return gaugeOne;
    }

    @Gauge(name = "gaugeMethod", unit = MetricUnits.NONE, tags = {"number=two"})
    public long getGaugeTwo() {
        return gaugeTwo;
    }

    public void setGaugeOne(long gauge) {
        this.gaugeOne = gauge;
    }

    public void setGaugeTwo(long gauge) {
        this.gaugeTwo = gauge;
    }
}
