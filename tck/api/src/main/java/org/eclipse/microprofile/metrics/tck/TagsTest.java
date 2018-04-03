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

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TagsTest {

    private Metadata metadata;

    @Before
    public void setUp() {
        this.metadata = Metadata.builder().withName("count").withDisplayName("countMe")
                .withDescription("countMe tags test").withType(MetricType.COUNTER)
                .withUnit(MetricUnits.PERCENT).addTag("colour=blue").build();
    }

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void tagsTest() {
        Assert.assertNotNull(metadata);
        Assert.assertTrue(metadata.getTags().containsValue("blue"));
    }

    @Test
    public void addTagsTest() {

        Metadata metadata2 = Metadata.builder(metadata)
                .addTags("colour=green,size=medium").addTag("number=5").build();

        Assert.assertNotNull(metadata2);
        Assert.assertTrue(metadata2.getTags().containsKey("size"));
        Assert.assertTrue(metadata2.getTags().containsValue("green"));
        Assert.assertFalse(metadata2.getTags().containsValue("blue"));
        Assert.assertTrue(metadata2.getTags().containsKey("number"));
    }

}
