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
package org.eclipse.microprofile.metrics.tck.metrics;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class CountedMethodTagBeanTest {

    private final static String COUNTER_NAME = "countedMethod";

    private final static Tag NUMBER_ONE_TAG = new Tag("number", "one");
    private final static Tag NUMBER_TWO_TAG = new Tag("number", "two");

    private static MetricID counterOneMID;
    private static MetricID counterTwoMID;

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(CountedMethodTagBean.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private CountedMethodTagBean bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API. Running a managed arquillian container will result with
         * the MetricID being created in a client process that does not contain the MPConfig impl.
         *
         * This will cause client instantiated MetricIDs to throw an exception. (i.e the global MetricIDs)
         */
        counterOneMID = new MetricID(COUNTER_NAME, NUMBER_ONE_TAG);
        counterTwoMID = new MetricID(COUNTER_NAME, NUMBER_TWO_TAG);
    }

    @Test
    @InSequence(1)
    public void counterTagMethodsRegistered() {
        assertThat("Counter is not registered correctly", registry.getCounter(counterOneMID), notNullValue());
        assertThat("Counter is not registered correctly", registry.getCounter(counterTwoMID), notNullValue());
    }

    @Test
    @InSequence(2)
    public void countedTagMethodNotCalledYet(
            @Metric(name = "countedMethod", absolute = true, tags = {"number=one"}) Counter instanceOne,
            @Metric(name = "countedMethod", absolute = true, tags = {"number=two"}) Counter instanceTwo) {
        Counter counterOne = registry.getCounter(counterOneMID);
        Counter counterTwo = registry.getCounter(counterTwoMID);

        assertThat("Counter is not registered correctly", counterOne, notNullValue());
        assertThat("Counter is not registered correctly", counterTwo, notNullValue());

        // Make sure that the counter registered and the bean instance are the same
        assertThat("Counter and bean instance are not equal", instanceOne, is(equalTo(counterOne)));
        assertThat("Counter and bean instance are not equal", instanceTwo, is(equalTo(counterTwo)));
    }
}
