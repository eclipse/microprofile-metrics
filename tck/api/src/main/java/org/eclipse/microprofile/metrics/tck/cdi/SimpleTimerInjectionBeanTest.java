/*
 * ********************************************************************
 *  Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.tck.cdi;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.tck.metrics.SimplyTimedMethodBean3;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class SimpleTimerInjectionBeanTest {

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClass(SimplyTimedMethodBean3.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SimplyTimedMethodBean3 bean;

    @Inject
    @Metric(absolute = true, name = "org.eclipse.microprofile.metrics.tck.metrics.SimplyTimedMethodBean3.simplyTimedMethod")
    private SimpleTimer simpleTimer;

    @Test
    @InSequence(1)
    public void simplyTimedMethodNotCalledYet() {
        // Make sure that the timer hasn't been called yet
        assertThat("SimpleTimer count is incorrect", simpleTimer.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callSimplyTimedMethodOnce() throws InterruptedException {
        // Call the timed method and assert it's been timed
        bean.simplyTimedMethod();

        // Make sure that the timer has been called
        assertThat("SimpleTimer count is incorrect", simpleTimer.getCount(), is(equalTo(1L)));
    }
}
