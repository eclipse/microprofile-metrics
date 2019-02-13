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

import org.eclipse.microprofile.metrics.Counter;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CounterFieldTagBeanTest {

    private final static String COUNTER_NAME = MetricRegistry.name(CounterFieldTagBean.class, "counterName");
    
    private final static Tag NUMBER_TWO_TAG = new Tag("number", "two");
    private final static Tag NUMBER_THREE_TAG = new Tag("number", "three");
        
    private final static Tag COLOUR_RED_TAG = new Tag("colour", "red"); 
    private final static Tag COLOUR_BLUE_TAG = new Tag("colour", "blue"); 
        
    private static MetricID counterMID ;
    private static MetricID counterTwoMID;
    private static MetricID counterThreeMID;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(CounterFieldTagBean.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private CounterFieldTagBean bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API.
         * Running a managed arquillian container will result
         * with the MetricID being created in a client process
         * that does not contain the MPConfig impl.
         * 
         * This will cause client instantiated MetricIDs to 
         * throw an exception. (i.e the global MetricIDs)
         */
        counterMID = new MetricID(COUNTER_NAME);
        counterTwoMID = new MetricID(COUNTER_NAME, NUMBER_TWO_TAG, COLOUR_RED_TAG);
        counterThreeMID = new MetricID(COUNTER_NAME, NUMBER_THREE_TAG, COLOUR_BLUE_TAG);
    }
    
    @Test
    @InSequence(1)
    public void counterTagFieldsRegistered() {      
        
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterMID));
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterTwoMID));
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterThreeMID));
    }

    @Test
    @InSequence(2)
    public void incrementCounterTagFields() {
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterMID));
        Counter counterOne = registry.getCounters().get(counterMID);
        
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterTwoMID));
        Counter counterTwo = registry.getCounters().get(counterTwoMID);
        
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(counterThreeMID));
        Counter counterThree = registry.getCounters().get(counterThreeMID);

        // Call the increment method and assert the counter is up-to-date
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        bean.incrementOne(value);
        
        long valueTwo = Math.round(Math.random() * Long.MAX_VALUE);
        bean.incrementTwo(valueTwo);
        
        long valueThree = Math.round(Math.random() * Long.MAX_VALUE);
        bean.incrementThree(valueThree);
        
        assertThat("Counter value is incorrect", counterOne.getCount(), is(equalTo(value)));
        assertThat("Counter value is incorrect", counterTwo.getCount(), is(equalTo(valueTwo)));
        assertThat("Counter value is incorrect", counterThree.getCount(), is(equalTo(valueThree)));
    }
}
