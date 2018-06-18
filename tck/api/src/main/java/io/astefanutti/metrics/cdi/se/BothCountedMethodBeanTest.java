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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BothCountedMethodBeanTest {


    private static final String METHOD_NAME =  "countedMethodOne";

    private static final Class<BothCountedMethodBean> CLAZZ = BothCountedMethodBean.class;

    private static final String[] COUNTER_NAMES_A = new String[] {"bla",MetricsUtil.absoluteMetricName(CLAZZ,
                                                                                     CLAZZ.getSimpleName(),
                                                                                     METHOD_NAME)};
    private static final Set<String> COUNTER_NAMES = new HashSet<>(Arrays.asList(COUNTER_NAMES_A));


    private static final MetricFilter METHOD_COUNTERS_FILTER = (name, metric) -> COUNTER_NAMES.contains(name) ;


    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                // Test bean
                .addClasses(CLAZZ, MetricsUtil.class)
                // Bean archive deployment descriptor
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private BothCountedMethodBean bean;

    @Test
    @InSequence(1)
    public void countedMethodsNotCalledYet() {
        System.out.println("test 1");
        assertThat("HitCounters are not registered correctly",
                   registry.getHitCounters().keySet(), is(COUNTER_NAMES));
        assertThat("ParallelCounters are not registered correctly",
                   registry.getParallelCounters().keySet(), is(COUNTER_NAMES));

        // Make sure that the counters haven't been incremented
        assertThat("HitCounter counts are incorrect", registry.getHitCounters(METHOD_COUNTERS_FILTER).values(),
                   everyItem(Matchers.hasProperty
            ("count", equalTo(0L))));
        assertThat("ParallelCounter counts are incorrect", registry.getParallelCounters(METHOD_COUNTERS_FILTER).values(),
                   everyItem(Matchers.hasProperty
            ("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callCountedMethodsOnce() {
        System.out.println("test 2");
        assertThat("Counters are not registered correctly", registry.getHitCounters().keySet(), is(COUNTER_NAMES));
        assertThat("Counters are not registered correctly", registry.getParallelCounters().keySet(), is(COUNTER_NAMES));


        // Call the counted methods and assert they've been incremented
        bean.countedMethodOne();
        bean.countedMethodTwo();

        // Make sure that the counters have been incremented
        assertThat("Method counter counts are incorrect", registry.getHitCounters(METHOD_COUNTERS_FILTER).values(),
                everyItem(Matchers.hasProperty("count", equalTo(1L))));
        assertThat("Method counter counts are incorrect", registry.getParallelCounters(METHOD_COUNTERS_FILTER).values(),
                everyItem(Matchers.hasProperty("count", equalTo(0L))));
    }
}
