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

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.NamedMetadata;
import org.eclipse.microprofile.metrics.annotation.NamedMetadatas;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.DeploymentException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * There is a NamedMetadata object with reusable=false and two counters with the same MetricID that reference it.
 * This is not valid and should be treated as a deployment error.
 */
@RunWith(Arquillian.class)
public class NamedMetadataNotReusableTest {

    @NamedMetadatas({
        @NamedMetadata(name = "my-metadata",
            metricName = "reusable-counter",
            type = MetricType.COUNTER)
    })
    public static class BeanWithNotReusableNamedMetadata {

        @Counted(metadata = "my-metadata", tags = "a=b")
        public void countedMethod() {

        }

        @Counted(metadata = "my-metadata", tags = "a=b")
        public void countedMethod2() {

        }

    }

    @Deployment(name = "not-reusable-wrong-deployment", managed = false)
    @ShouldThrowException(value = DeploymentException.class)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(BeanWithNotReusableNamedMetadata.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private Deployer deployer;

    @Test
    public void verifyError() {
        deployer.deploy("not-reusable-wrong-deployment");
    }

}
