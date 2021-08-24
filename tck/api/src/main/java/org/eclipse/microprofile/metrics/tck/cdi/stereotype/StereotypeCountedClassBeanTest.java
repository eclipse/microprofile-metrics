/*
 **********************************************************************
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.tck.cdi.stereotype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.tck.cdi.stereotype.stereotypes.CountMe;
import org.eclipse.microprofile.metrics.tck.cdi.stereotype.stereotypes.CountMeWithSpecifiedMetadata;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class StereotypeCountedClassBeanTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(StereotypeCountedClassBean.class,
                        StereotypeCountedClassBeanWithSpecifiedMetadata.class,
                        CountMe.class,
                        CountMeWithSpecifiedMetadata.class);
    }

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private StereotypeCountedClassBean bean;

    @Inject
    private StereotypeCountedClassBeanWithSpecifiedMetadata beanWithSpecifiedMetadata;

    @Test
    public void testPlainAnnotation() {
        MetricID constructorMetricId =
                new MetricID(StereotypeCountedClassBean.class.getName() + ".StereotypeCountedClassBean");
        assertNotNull(metricRegistry.getCounter(constructorMetricId));
        MetricID methodMetricId = new MetricID(StereotypeCountedClassBean.class.getName() + ".foo");
        assertNotNull(metricRegistry.getCounter(methodMetricId));
        bean.foo();
        assertEquals(1, metricRegistry.getCounter(methodMetricId).getCount());
    }

    @Test
    public void testWithMetadata() {
        String constructorMetricName =
                "org.eclipse.microprofile.metrics.tck.cdi.stereotype.bloop.StereotypeCountedClassBeanWithSpecifiedMetadata";
        MetricID constructorMetricId = new MetricID(constructorMetricName);
        assertNotNull(metricRegistry.getCounter(constructorMetricId));
        Metadata constructorMetadata = metricRegistry.getMetadata(constructorMetricName);
        assertEquals("description", constructorMetadata.description().orElse(null));
        assertEquals("displayName", constructorMetadata.getDisplayName());

        String methodMetricName = "org.eclipse.microprofile.metrics.tck.cdi.stereotype.bloop.foo";
        MetricID methodMetricId = new MetricID(methodMetricName);
        assertNotNull(metricRegistry.getCounter(methodMetricId));
        Metadata methodMetadata = metricRegistry.getMetadata(methodMetricName);
        assertEquals("description", methodMetadata.description().orElse(null));
        assertEquals("displayName", methodMetadata.getDisplayName());

        beanWithSpecifiedMetadata.foo();
        assertEquals(1, metricRegistry.getCounter(methodMetricId).getCount());
    }

}
