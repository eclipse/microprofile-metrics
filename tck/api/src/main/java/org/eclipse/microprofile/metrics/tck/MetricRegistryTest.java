/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MetricRegistryTest {
	
	@Inject
	@Metric(name="nameTest", absolute=true)
	Counter nameTest;
	
	@Inject
	Counter countTemp;
	
	@Inject
	Histogram histoTemp;
	
	@Inject
	Timer timerTemp;
	
	@Inject
	Meter meterTemp;
	
	@Inject
	MetricRegistry metrics;
	
	@Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

	@Test
	@InSequence(1)
	public void nameTest() {
		Assert.assertNotNull(metrics);
		Assert.assertTrue(metrics.getNames().contains("nameTest"));
	}
	
	@Test
	@InSequence(2)
	public void registerTest() {
		metrics.register("regCountTemp", countTemp);
		Assert.assertTrue(metrics.getCounters().containsKey("regCountTemp"));
		
		metrics.register("regHistoTemp", histoTemp);
		Assert.assertTrue(metrics.getHistograms().containsKey("regHistoTemp"));
		
		metrics.register("regTimerTemp", timerTemp);
		Assert.assertTrue(metrics.getTimers().containsKey("regTimerTemp"));
		
		metrics.register("regMeterTemp", meterTemp);
		Assert.assertTrue(metrics.getMeters().containsKey("regMeterTemp"));
	}
	

	@Test
	@InSequence(3)
	public void removeTest() {
		metrics.remove("nameTest");
		Assert.assertFalse(metrics.getNames().contains("nameTest"));
	}

}
