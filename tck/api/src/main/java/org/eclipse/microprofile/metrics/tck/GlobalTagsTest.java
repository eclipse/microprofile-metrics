/*
 **********************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *               2010-2013 Coda Hale, Yammer.com
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

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Test for obtaining global tags from MP_METRICS_TAGS env variable.
 */
@RunWith(Arquillian.class)
public class GlobalTagsTest {

    @Inject
    private MetricRegistry registry;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void cleanupApplicationMetrics() {
        registry.removeMatching(MetricFilter.ALL);
    }

    /**
     * This expects that there is a MP_METRICS_TAGS env variable with value 'tier=integration', as described in running_the_tck.asciidoc
     */
    @Test
    public void fromEnvVariable() {
        Metadata metadata = new Metadata("mycounter", MetricType.COUNTER);
        metadata.addTags("key1=value1");

        registry.counter(metadata);

        Metadata actualMetadata = registry.getMetadata().get("mycounter");
        assertEquals("value1", actualMetadata.getTags().get("key1"));
        assertEquals("integration", actualMetadata.getTags().get("tier"));
    }

}
