/*
 **********************************************************************
 * Copyright (c) 2017, 2020 Contributors to the Eclipse Foundation
 *               2010-2013 Coda Hale, Yammer.com
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

package org.eclipse.microprofile.metrics.tck.metrics;

import static org.hamcrest.Matchers.lessThan;

import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.tck.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class MeterTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(TestUtils.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Meter injectedMeter;

    @Inject
    private MetricRegistry registry;

    @Test
    public void testCount() throws Exception {
        // test mark()
        long countBefore = injectedMeter.getCount();
        injectedMeter.mark();
        long countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore + 1, countAfter);

        // test mark(2)
        countBefore = injectedMeter.getCount();
        injectedMeter.mark(2);
        countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore + 2, countAfter);

        // test mark(-3)
        countBefore = injectedMeter.getCount();
        injectedMeter.mark(-3);
        countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore - 3, countAfter);
    }

    @Test
    public void testRates() throws Exception {

        int count = 100;
        int markSeconds = 60;
        int delaySeconds = 15;

        Meter meter = registry.meter("testMeterRatesLong");

        for (int i = 0; i < markSeconds; i++) {
            meter.mark(count);
            Thread.sleep(1000);
        }

        // All rates should be around the value of count
        TestUtils.assertEqualsWithTolerance(count, meter.getMeanRate());
        TestUtils.assertEqualsWithTolerance(count, meter.getOneMinuteRate());
        TestUtils.assertEqualsWithTolerance(count, meter.getFiveMinuteRate());
        TestUtils.assertEqualsWithTolerance(count, meter.getFifteenMinuteRate());

        Thread.sleep(delaySeconds * 1000);

        // Approximately calculate what the expected mean should be
        // and let the tolerance account for the delta
        double expectedMean = count * ((double) markSeconds / (markSeconds + delaySeconds));
        TestUtils.assertEqualsWithTolerance(expectedMean, meter.getMeanRate());

        // After a delay, we expect some decay of values
        Assert.assertThat(meter.getOneMinuteRate(), lessThan((double) count));
        Assert.assertThat(meter.getFiveMinuteRate(), lessThan((double) count));
        Assert.assertThat(meter.getFifteenMinuteRate(), lessThan((double) count));

    }
}
