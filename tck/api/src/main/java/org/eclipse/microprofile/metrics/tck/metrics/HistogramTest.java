/*
 **********************************************************************
 * Copyright (c) 2017, 2022 Contributors to the Eclipse Foundation
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

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Snapshot;
import org.eclipse.microprofile.metrics.Snapshot.PercentileValue;
import org.eclipse.microprofile.metrics.tck.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class HistogramTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(TestUtils.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Histogram histogramInt;

    @Inject
    private MetricRegistry metrics;

    private static Histogram histogramLong = null;

    private static boolean isInitialized = false;
    final static int[] SAMPLE_INT_DATA = {0, 1, 2, 2, 2, 3, 3, 3, 3, 3, 4, 5, 5, 6, 7, 7, 7, 8, 9, 9, 10, 11, 11, 12,
            12,
            12, 12, 13, 13, 13, 13, 14, 14, 15, 15, 17, 18, 18, 20, 20, 20, 21, 22, 22, 22, 24, 24, 25, 25, 27, 27, 27,
            27, 27, 27, 27, 28, 28, 29, 30, 31, 31, 32, 32, 33, 33, 36, 36, 36, 36, 37, 38, 38, 38, 39, 40, 40, 41, 42,
            42, 42, 43, 44, 44, 44, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 47, 47, 47, 48, 48, 49, 49, 50, 51, 52, 52,
            52, 53, 54, 54, 55, 56, 56, 57, 57, 59, 59, 60, 61, 61, 62, 62, 63, 64, 64, 64, 65, 66, 66, 66, 67, 67, 68,
            68, 70, 71, 71, 71, 71, 72, 72, 72, 72, 73, 73, 74, 74, 74, 75, 75, 76, 76, 76, 77, 78, 78, 78, 80, 80, 81,
            82, 82, 82, 83, 83, 84, 84, 85, 87, 87, 88, 88, 88, 89, 89, 89, 89, 90, 91, 92, 92, 92, 93, 94, 95, 95, 95,
            96, 96, 96, 96, 97, 97, 97, 97, 98, 98, 98, 99, 99};

    final static long[] SAMPLE_LONG_DATA = {0, 10, 20, 20, 20, 30, 30, 30, 30, 30, 40, 50, 50, 60, 70, 70, 70, 80, 90,
            90, 100, 110, 110, 120, 120, 120, 120, 130, 130, 130, 130, 140, 140, 150, 150, 170, 180, 180, 200, 200, 200,
            210, 220, 220, 220, 240, 240, 250, 250, 270, 270, 270, 270, 270, 270, 270, 280, 280, 290, 300, 310, 310,
            320, 320, 330, 330, 360, 360, 360, 360, 370, 380, 380, 380, 390, 400, 400, 410, 420, 420, 420, 430, 440,
            440, 440, 450, 450, 450, 460, 460, 460, 460, 470, 470, 470, 470, 470, 470, 480, 480, 490, 490, 500, 510,
            520, 520, 520, 530, 540, 540, 550, 560, 560, 570, 570, 590, 590, 600, 610, 610, 620, 620, 630, 640, 640,
            640, 650, 660, 660, 660, 670, 670, 680, 680, 700, 710, 710, 710, 710, 720, 720, 720, 720, 730, 730, 740,
            740, 740, 750, 750, 760, 760, 760, 770, 780, 780, 780, 800, 800, 810, 820, 820, 820, 830, 830, 840, 840,
            850, 870, 870, 880, 880, 880, 890, 890, 890, 890, 900, 910, 920, 920, 920, 930, 940, 950, 950, 950, 960,
            960, 960, 960, 970, 970, 970, 970, 980, 980, 980, 990, 990};

    @Before
    public void initData() {
        if (isInitialized) {
            return;
        }

        histogramLong = metrics.histogram("test.longData.histogram");
        for (int i : SAMPLE_INT_DATA) {
            histogramInt.update(i);
        }
        for (long i : SAMPLE_LONG_DATA) {
            histogramLong.update(i);
        }
        isInitialized = true;
    }

    @Test
    public void testMetricRegistry() throws Exception {
        String histogramIntName = "org.eclipse.microprofile.metrics.tck.metrics.HistogramTest.histogramInt";
        String histogramLongName = "test.longData.histogram";

        MetricID histogramIntNameMetricID = new MetricID(histogramIntName);
        MetricID histogramLongNameMetricID = new MetricID(histogramLongName);

        Histogram histogramInt = metrics.getHistogram(histogramIntNameMetricID);
        Histogram histogramLong = metrics.getHistogram(histogramLongNameMetricID);

        assertThat("Histogram is not registered correctly", histogramInt, notNullValue());
        assertThat("Histogram is not registered correctly", histogramLong, notNullValue());

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.5);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.5);

        TestUtils.assertEqualsWithTolerance(48, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(480, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testCount() throws Exception {
        Assert.assertEquals(200, histogramInt.getCount());
        Assert.assertEquals(200, histogramLong.getCount());
    }

    @Test
    public void testSum() throws Exception {
        Assert.assertEquals(10127, histogramInt.getSum());
        Assert.assertEquals(101270, histogramLong.getSum());
    }

    @Test
    public void testSnapshotPercentileValuesPresent() throws Exception {

        PercentileValue[] percentileValuesHistInt = histogramInt.getSnapshot().percentileValues();
        // Check that there are 6 percentiles - [0.75,0.95,0.98,0.99,0.999]
        Assert.assertTrue(percentileValuesHistInt.length == 6);

        int countDown = 6;
        for (PercentileValue pv : percentileValuesHistInt) {
            double percentile = pv.getPercentile();
            if (percentile == 0.5 ||
                    percentile == 0.75 ||
                    percentile == 0.95 ||
                    percentile == 0.98 ||
                    percentile == 0.99 ||
                    percentile == 0.999) {
                countDown--;
            }
        }
        Assert.assertTrue(countDown == 0);

        PercentileValue[] percentileValuesHisLong = histogramLong.getSnapshot().percentileValues();
        // Check that there are 6 percentiles - [0.5,0.75,0.95,0.98,0.99,0.999]
        Assert.assertTrue(percentileValuesHisLong.length == 6);

        countDown = 6;
        for (PercentileValue pv : percentileValuesHisLong) {
            double percentile = pv.getPercentile();
            if (percentile == 0.5 ||
                    percentile == 0.75 ||
                    percentile == 0.95 ||
                    percentile == 0.98 ||
                    percentile == 0.99 ||
                    percentile == 0.999) {
                countDown--;
            }
        }
        Assert.assertTrue(countDown == 0);
    }

    @Test
    public void testSnapshot50thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.5);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.5);

        TestUtils.assertEqualsWithTolerance(48, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(480, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshot75thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.75);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.75);

        TestUtils.assertEqualsWithTolerance(75, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(750, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshot95thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.95);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.95);

        TestUtils.assertEqualsWithTolerance(96, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(960, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshot98thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.98);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.98);

        TestUtils.assertEqualsWithTolerance(98, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(980, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshot99thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.99);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.99);

        TestUtils.assertEqualsWithTolerance(98, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(980, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshot999thPercentile() throws Exception {

        PercentileValue histogramIntPercentileValue = getPercentileValueAt(histogramInt, 0.999);
        PercentileValue histogramLongPercentileValue = getPercentileValueAt(histogramLong, 0.999);

        TestUtils.assertEqualsWithTolerance(99, histogramIntPercentileValue.getValue());
        TestUtils.assertEqualsWithTolerance(990, histogramLongPercentileValue.getValue());
    }

    @Test
    public void testSnapshotMax() throws Exception {
        Assert.assertEquals(99, histogramInt.getSnapshot().getMax(), 0.0);
        Assert.assertEquals(990, histogramLong.getSnapshot().getMax(), 0.0);
    }

    @Test
    public void testSnapshotMean() throws Exception {
        TestUtils.assertEqualsWithTolerance(50.6, histogramInt.getSnapshot().getMean());
        TestUtils.assertEqualsWithTolerance(506.3, histogramLong.getSnapshot().getMean());
    }

    @Test
    public void testSnapshotSize() throws Exception {
        Assert.assertEquals(200, histogramInt.getSnapshot().size());
        Assert.assertEquals(200, histogramLong.getSnapshot().size());
    }

    private static PercentileValue getPercentileValueAt(Histogram histo, double percentile) {
        Snapshot snapshot = histo.getSnapshot();

        PercentileValue percentileValue = null;
        for (PercentileValue pv : snapshot.percentileValues()) {
            if (pv.getPercentile() == percentile) {
                percentileValue = pv;
                break;
            }
        }
        assertNotNull(percentileValue);
        return percentileValue;
    }

}
