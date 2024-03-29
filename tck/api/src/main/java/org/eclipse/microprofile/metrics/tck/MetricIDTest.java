/*
 **********************************************************************
 * Copyright (c) 2019, 2022 Contributors to the Eclipse Foundation
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class MetricIDTest {

    @Inject
    private MetricRegistry registry;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    @InSequence(1)
    public void removalTest() {

        Tag tagEarth = new Tag("planet", "earth");
        Tag tagRed = new Tag("colour", "red");
        Tag tagBlue = new Tag("colour", "blue");

        String counterName = "org.eclipse.microprofile.metrics.tck.MetricIDTest.counterColour";
        String counterNameNoTag = "org.eclipse.microprofile.metrics.tck.MetricIDTest.counter";

        Counter counter = registry.counter(counterNameNoTag);
        Counter counterRed = registry.counter(counterName, tagEarth, tagRed);
        Counter counterBlue = registry.counter(counterName, tagEarth, tagBlue);

        MetricID counterMID = new MetricID(counterNameNoTag);
        MetricID counterRedMID = new MetricID(counterName, tagEarth, tagRed);
        MetricID counterBlueMID = new MetricID(counterName, tagEarth, tagRed);

        // check multi-dimensional metrics are registered
        assertThat("Counter is not registered correctly", registry.getCounter(counterMID), notNullValue());
        assertThat("Counter is not registered correctly", registry.getCounter(counterRedMID), notNullValue());
        assertThat("Counter is not registered correctly", registry.getCounter(counterBlueMID), notNullValue());

        // remove one metric
        registry.remove(counterMID);
        assertThat("Registry did not remove metric", registry.getCounters().size(), equalTo(2));
        assertThat("Counter is not registered correctly", registry.getCounter(counterMID), nullValue());

        // remove all metrics with the given name
        registry.remove(counterName);
        assertThat("Counter is not registered correctly", registry.getCounter(counterRedMID), nullValue());
        assertThat("Counter is not registered correctly", registry.getCounter(counterBlueMID), nullValue());

    }

}
