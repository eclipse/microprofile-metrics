/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.metrics.tck;

import static org.hamcrest.Matchers.lessThan;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.Timer.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TimerTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(TestUtils.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    private static Timer globalTimer = null;

    private static boolean isInitialized = false;

    final static long[] SAMPLE_LONG_DATA = { 0, 10, 20, 20, 20, 30, 30, 30, 30, 30, 40, 50, 50, 60, 70, 70, 70, 80, 90,
            90, 100, 110, 110, 120, 120, 120, 120, 130, 130, 130, 130, 140, 140, 150, 150, 170, 180, 180, 200, 200, 200,
            210, 220, 220, 220, 240, 240, 250, 250, 270, 270, 270, 270, 270, 270, 270, 280, 280, 290, 300, 310, 310,
            320, 320, 330, 330, 360, 360, 360, 360, 370, 380, 380, 380, 390, 400, 400, 410, 420, 420, 420, 430, 440,
            440, 440, 450, 450, 450, 460, 460, 460, 460, 470, 470, 470, 470, 470, 470, 480, 480, 490, 490, 500, 510,
            520, 520, 520, 530, 540, 540, 550, 560, 560, 570, 570, 590, 590, 600, 610, 610, 620, 620, 630, 640, 640,
            640, 650, 660, 660, 660, 670, 670, 680, 680, 700, 710, 710, 710, 710, 720, 720, 720, 720, 730, 730, 740,
            740, 740, 750, 750, 760, 760, 760, 770, 780, 780, 780, 800, 800, 810, 820, 820, 820, 830, 830, 840, 840,
            850, 870, 870, 880, 880, 880, 890, 890, 890, 890, 900, 910, 920, 920, 920, 930, 940, 950, 950, 950, 960,
            960, 960, 960, 970, 970, 970, 970, 980, 980, 980, 990, 990 };

    @Before
    public void initData() {
        if (isInitialized) {
            return;
        }

        globalTimer = registry.timer("test.longData.timer");

        for (long i : SAMPLE_LONG_DATA) {
            globalTimer.update(i, TimeUnit.NANOSECONDS);
        }
        isInitialized = true;
    }

    @Test
    @InSequence(1)
    public void testRate() throws Exception {

        int markSeconds = 30;
        int delaySeconds = 15;
        
        Timer timer = registry.timer("testRate");
        
        // Call update ~1/sec
        for (int i = 0; i < markSeconds; i++) {
            timer.update(1, TimeUnit.SECONDS);
            Thread.sleep(1000);
        }
        
        // All rates should be around the value of ~1/sec
        TestUtils.assertEqualsWithTolerance(1.0, timer.getMeanRate());
        TestUtils.assertEqualsWithTolerance(1.0, timer.getOneMinuteRate());
        TestUtils.assertEqualsWithTolerance(1.0, timer.getFiveMinuteRate());
        TestUtils.assertEqualsWithTolerance(1.0, timer.getFifteenMinuteRate());
        
        Thread.sleep(delaySeconds * 1000);
        
        // Approximately calculate what the expected mean should be
        // and let the tolerance account for the delta
        double expectedMean = ((double) markSeconds/(markSeconds + delaySeconds));
        TestUtils.assertEqualsWithTolerance(expectedMean, timer.getMeanRate());
        
        // After a delay, we expect some decay of values
        Assert.assertThat(timer.getOneMinuteRate(), lessThan(1.0));
        Assert.assertThat(timer.getFiveMinuteRate(), lessThan(1.0));
        Assert.assertThat(timer.getFifteenMinuteRate(), lessThan(1.0));
    }
    
    @Test
    @InSequence(2)
    public void testTime() throws Exception {
        Timer timer = registry.timer("testTime");

        double beforeStartTime = System.nanoTime();
        Context context = timer.time();
        double afterStartTime = System.nanoTime();
        Thread.sleep(1000);
        
        double beforeStopTime = System.nanoTime();
        double time = context.stop();
        double afterStopTime = System.nanoTime();
        

        double delta = (afterStartTime - beforeStartTime) + (afterStopTime - beforeStopTime);
        Assert.assertEquals(beforeStopTime - beforeStartTime, time, delta);
    }

    @Test
    @InSequence(3)
    public void testTimerRegistry() throws Exception {
        String timerLongName = "test.longData.timer";
        String timerTimeName = "testTime";
        
        MetricID timerLongNameMetricID = new MetricID(timerLongName);
        MetricID timerTimeNameMetricID = new MetricID(timerTimeName);
        
        SortedMap<MetricID, Timer> timers = registry.getTimers();
        
        Assert.assertTrue(timers.size() > 0);

        Assert.assertTrue(timers.containsKey(timerLongNameMetricID));
        Assert.assertTrue(timers.containsKey(timerTimeNameMetricID));

        TestUtils.assertEqualsWithTolerance(480, timers.get(timerLongNameMetricID).getSnapshot().getValue(0.5));
    }

    @Test
    @InSequence(4)
    public void timesCallableInstances() throws Exception {
        Timer timer = registry.timer("testCallable");
        final String value = timer.time(() -> "one");

        Assert.assertEquals(timer.getCount(), 1);

        Assert.assertEquals(value, "one");
    }

    @Test
    @InSequence(5)
    public void timesRunnableInstances() throws Exception {
        Timer timer = registry.timer("testRunnable");
        final AtomicBoolean called = new AtomicBoolean();
        timer.time(() -> called.set(true));

        Assert.assertEquals(timer.getCount(), 1);

        Assert.assertEquals(called.get(), true);
    }

    @Test
    public void testSnapshotValues() throws Exception {
        Assert.assertArrayEquals(
                "The globalTimer does not contain the expected values: " + Arrays.toString(SAMPLE_LONG_DATA),
                SAMPLE_LONG_DATA, globalTimer.getSnapshot().getValues());
    }

    @Test
    public void testSnapshot75thPercentile() throws Exception {
        TestUtils.assertEqualsWithTolerance(750, globalTimer.getSnapshot().get75thPercentile());
    }

    @Test
    public void testSnapshot95thPercentile() throws Exception {
        TestUtils.assertEqualsWithTolerance(960, globalTimer.getSnapshot().get95thPercentile());
    }

    @Test
    public void testSnapshot98thPercentile() throws Exception {
        TestUtils.assertEqualsWithTolerance(980, globalTimer.getSnapshot().get98thPercentile());
    }

    @Test
    public void testSnapshot99thPercentile() throws Exception {
        TestUtils.assertEqualsWithTolerance(980, globalTimer.getSnapshot().get99thPercentile());
    }

    @Test
    public void testSnapshot999thPercentile() throws Exception {
        TestUtils.assertEqualsWithTolerance(990, globalTimer.getSnapshot().get999thPercentile());
    }

    @Test
    public void testSnapshotMax() throws Exception {
        Assert.assertEquals(990, globalTimer.getSnapshot().getMax());
    }

    @Test
    public void testSnapshotMin() throws Exception {
        Assert.assertEquals(0, globalTimer.getSnapshot().getMin());
    }

    @Test
    public void testSnapshotMean() throws Exception {
        TestUtils.assertEqualsWithTolerance(506.3, globalTimer.getSnapshot().getMean());
    }

    @Test
    public void testSnapshotMedian() throws Exception {
        TestUtils.assertEqualsWithTolerance(480, globalTimer.getSnapshot().getMedian());
    }

    @Test
    public void testSnapshotStdDev() throws Exception {
        TestUtils.assertEqualsWithTolerance(294.3, globalTimer.getSnapshot().getStdDev());
    }

    @Test
    public void testSnapshotSize() throws Exception {
        Assert.assertEquals(200, globalTimer.getSnapshot().size());
    }
}
