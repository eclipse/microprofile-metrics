/*
 **********************************************************************
 * Copyright (c) 2017, 2022 Contributors to the Eclipse Foundation
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

import static org.junit.Assert.assertNotNull;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.RegistryScope;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class MetricRegistryTest {

    private static final String CUSTOM_SCOPE = "customScope";

    @Inject
    @Metric(name = "nameTest", absolute = true)
    private Counter nameTest;

    @Inject
    private MetricRegistry metrics;

    @Inject
    @RegistryScope(scope = MetricRegistry.BASE_SCOPE)
    private MetricRegistry baseMetrics;

    @Inject
    @RegistryScope(scope = MetricRegistry.VENDOR_SCOPE)
    private MetricRegistry vendorMetrics;

    @Inject
    @RegistryScope(scope = CUSTOM_SCOPE)
    private MetricRegistry customScope;

    @Inject
    @RegistryType(type = MetricRegistry.Type.BASE)
    private MetricRegistry baseMetrics_RegistryType;

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    private MetricRegistry vendorMetrics_RegistryType;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    private MetricRegistry applicationMetrics_RegistryType;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    @InSequence(1)
    public void nameTest() {
        Assert.assertNotNull(metrics);
        Assert.assertNotNull(metrics.getMetadata("nameTest"));
    }

    @Test
    @InSequence(3)
    public void removeTest() {
        metrics.remove("nameTest");
        Assert.assertNull(metrics.getMetadata("nameTest"));
    }

    @Test
    @InSequence(4)
    public void useExistingMetaDataTest() {
        String metricName = "counterFoo";

        Tag orangeTag = new Tag("colour", "orange");
        Tag purpleTag = new Tag("colour", "purple");

        // first to register a "complex" metadata
        metrics.counter(Metadata.builder().withName(metricName)
                .build(), orangeTag);

        // creates with a simple/template metadata or uses an existing one.
        metrics.counter(metricName, purpleTag);

        // check both counters have been registered
        assertExists(Counter.class, new MetricID(metricName, orangeTag));
        assertExists(Counter.class, new MetricID(metricName, purpleTag));
    }

    @Test
    @InSequence(5)
    public void testMetricRegistryScope() {
        Assert.assertEquals(MetricRegistry.APPLICATION_SCOPE, metrics.getScope());
        Assert.assertEquals(MetricRegistry.BASE_SCOPE, baseMetrics.getScope());
        Assert.assertEquals(MetricRegistry.VENDOR_SCOPE, vendorMetrics.getScope());
        Assert.assertEquals(CUSTOM_SCOPE, customScope.getScope());
    }

    @Test
    @InSequence(6)
    public void testMetricRegistryScopeDeprecatedRegistryType() {
        Assert.assertEquals(MetricRegistry.APPLICATION_SCOPE, applicationMetrics_RegistryType.getScope());
        Assert.assertEquals(MetricRegistry.BASE_SCOPE, baseMetrics_RegistryType.getScope());
        Assert.assertEquals(MetricRegistry.VENDOR_SCOPE, vendorMetrics_RegistryType.getScope());
    }

    @Test
    @InSequence(7)
    public void testMetricRegistryEquivalence() {
        Assert.assertEquals(metrics, applicationMetrics_RegistryType);
        Assert.assertEquals(baseMetrics, baseMetrics_RegistryType);
        Assert.assertEquals(vendorMetrics, vendorMetrics_RegistryType);
    }

    private void assertExists(Class<? extends org.eclipse.microprofile.metrics.Metric> expected, MetricID metricID) {
        assertNotNull("Metric expected to exist but was undefined: " + metricID, metrics.getMetric(metricID, expected));
    }
}
