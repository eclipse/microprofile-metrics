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
package io.astefanutti.metrics.cdi.se;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class HistogramTagFieldBeanTest {

    private final static String HISTOGRAM_NAME = MetricRegistry.name(HistogramTagFieldBean.class, "histogramName");

    private final static Tag NUMBER_ONE_TAG = new Tag("number", "one");
    private final static Tag NUMBER_TWO_TAG = new Tag("number", "two");
    
    private final static MetricID HISTOGRAM_ONE_METRICID = new MetricID(HISTOGRAM_NAME, NUMBER_ONE_TAG);
    private final static MetricID HISTOGRAM_TWO_METRICID = new MetricID(HISTOGRAM_NAME, NUMBER_TWO_TAG);
    
    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(HistogramTagFieldBean.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private HistogramTagFieldBean bean;

    @Test
    @InSequence(1)
    public void histogramTagFieldRegistered() {
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_ONE_METRICID));
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_TWO_METRICID));
    }
    
    @Test
    @InSequence(2)
    public void updateHistogramTagField() {
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_ONE_METRICID));
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_TWO_METRICID));
        
        Histogram histogramOne = registry.getHistograms().get(HISTOGRAM_ONE_METRICID);
        Histogram histogramTwo = registry.getHistograms().get(HISTOGRAM_TWO_METRICID);
        
        // Call the update method and assert the histogram is up-to-date
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        bean.updateOne(value);
        long valueTwo = Math.round(Math.random() * Long.MAX_VALUE);
        bean.updateTwo(valueTwo);
        
        assertThat("Histogram count is incorrect", histogramOne.getCount(), is(equalTo(1L)));
        assertThat("Histogram size is incorrect", histogramOne.getSnapshot().size(), is(equalTo(1)));
        assertThat("Histogram min value is incorrect", histogramOne.getSnapshot().getMin(), is(equalTo(value)));
        assertThat("Histogram max value is incorrect", histogramOne.getSnapshot().getMax(), is(equalTo(value)));
        

        assertThat("Histogram count is incorrect", histogramTwo.getCount(), is(equalTo(1L)));
        assertThat("Histogram size is incorrect", histogramTwo.getSnapshot().size(), is(equalTo(1)));
        assertThat("Histogram min value is incorrect", histogramTwo.getSnapshot().getMin(), is(equalTo(valueTwo)));
        assertThat("Histogram max value is incorrect", histogramTwo.getSnapshot().getMax(), is(equalTo(valueTwo)));
    }
}