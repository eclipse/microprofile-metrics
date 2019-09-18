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

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.NamedMetadata;
import org.eclipse.microprofile.metrics.annotation.NamedMetadatas;
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

/**
 * There is a NamedMetadata object with reusable=true and two counters with the same MetricID that reference it.
 * This is valid and should work.
 */
@RunWith(Arquillian.class)
public class NamedMetadataReusableTest {

    @NamedMetadatas({
        @NamedMetadata(name = "my-metadata",
            metricName = "reusable-counter",
            description = "awesome-description",
            displayName = "awesome-display-name",
            unit = "awesome-unit",
            type = MetricType.COUNTER,
            reusable = true)
    })
    public static class BeanWithReusableNamedMetadata {

        @Counted(metadata = "my-metadata", tags = "a=b")
        public void countedMethod() {

        }

        @Counted(metadata = "my-metadata", tags = "a=b")
        public void countedMethod2() {

        }

    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(BeanWithReusableNamedMetadata.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    BeanWithReusableNamedMetadata bean;

    @Inject
    MetricRegistry metricRegistry;

    @Test
    public void verifyCounter() {
        Metadata metadata = metricRegistry.getMetadata().get("reusable-counter");
        assertNotNull(metadata);
        assertEquals("awesome-description", metadata.getDescription().orElse(""));
        assertEquals("awesome-display-name", metadata.getDisplayName());
        assertEquals("awesome-unit", metadata.getUnit().orElse(""));

        bean.countedMethod();
        bean.countedMethod2();
        Counter counter = metricRegistry.getCounters().get(new MetricID("reusable-counter", new Tag("a", "b")));
        assertEquals(2, counter.getCount());
    }

}
