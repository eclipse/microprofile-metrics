/*
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * ********************************************************************
 *  Copyright (c) 2018 Contributors to the Eclipse Foundation
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
package io.astefanutti.metrics.cdi.se;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.astefanutti.metrics.cdi.se.util.MetricsUtil;
import java.util.Set;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.hamcrest.Matchers;
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
public class ConcurrentGaugedClassBeanTest {

    private static final String CONSTRUCTOR_NAME = "ConcurrentGaugedClassBean";

    private static final String[] METHOD_NAMES = { "countedMethodOne", "countedMethodTwo", "countedMethodProtected", "countedMethodPackagedPrivate" };

    private static final Set<String> C_GAUGED_NAMES = MetricsUtil.absoluteMetricNames(ConcurrentGaugedClassBean.class, "cGaugedClass", METHOD_NAMES,
                                                                                      CONSTRUCTOR_NAME);

    private static Set<MetricID> counterMIDs;

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                // Test bean
                .addClasses(ConcurrentGaugedClassBean.class, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private ConcurrentGaugedClassBean bean;

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
        counterMIDs = MetricsUtil.createMetricIDs(C_GAUGED_NAMES);
    }
    
    @Test
    @InSequence(1)
    public void countedMethodsNotCalledYet() {
        assertThat("Counters are not registered correctly", registry.getConcurrentGauges().keySet(), is(equalTo(counterMIDs)));
        // Make sure that the counters haven't been incremented
        assertThat("Concurrent Gauges max values are incorrect", registry.getConcurrentGauges().values(),
                   everyItem(Matchers.<ConcurrentGauge>hasProperty("max", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callCountedMethodsOnce() {
        assertThat("Counters are not registered correctly", registry.getConcurrentGauges().keySet(), is(equalTo(counterMIDs)));
        // Call the counted methods and assert they're back to zero
        bean.countedMethodOne();
        bean.countedMethodTwo();
        // Let's call the non-public methods as well
        bean.countedMethodProtected();
        bean.countedMethodPackagedPrivate();

        // Make sure that the counters are back to zero
        assertThat("Concurrent Gauges counts are incorrect", registry.getConcurrentGauges().values(),
                   everyItem(Matchers.<ConcurrentGauge>hasProperty("count", equalTo(0L))));
    }
}
