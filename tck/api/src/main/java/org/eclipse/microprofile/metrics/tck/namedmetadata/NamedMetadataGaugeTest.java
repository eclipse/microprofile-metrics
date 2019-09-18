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

package org.eclipse.microprofile.metrics.tck.namedmetadata;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.NamedMetadata;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class NamedMetadataGaugeTest {


    @NamedMetadata(name = "my-gauge-metadata",
        metricName = "my-gauge1",
        description = "awesome-gauge-description",
        displayName = "awesome-gauge-display-name",
        unit = "awesome-gauge-unit",
        type = MetricType.GAUGE)
    public static class BeanWithMetadataAnnotation {

        @Gauge(metadata = "my-gauge-metadata")
        public Long gaugedMethod() {
            return 1234L;
        }

    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(BeanWithMetadataAnnotation.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    MetricRegistry metricRegistry;

    @Inject
    BeanWithMetadataAnnotation bean;

    @Test
    public void verifyGauge() {
        bean.gaugedMethod(); // just to force instantiation of the bean and registration of the gauge

        Metadata metadata = metricRegistry.getMetadata().get("my-gauge1");
        assertNotNull(metadata);
        assertEquals("awesome-gauge-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-gauge-display-name", metadata.getDisplayName());
        assertEquals("awesome-gauge-unit", metadata.getUnit().orElse(""));

        org.eclipse.microprofile.metrics.Gauge gauge = metricRegistry.getGauges().get(new MetricID("my-gauge1"));
        assertEquals(1234L, gauge.getValue());
    }

}
