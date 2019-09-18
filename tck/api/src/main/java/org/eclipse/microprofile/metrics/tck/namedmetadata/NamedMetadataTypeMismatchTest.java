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

import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.NamedMetadata;
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

/**
 * NamedMetadata declares a different metric type than the annotation where it is references. This must fail the deployment.
 */
@RunWith(Arquillian.class)
public class NamedMetadataTypeMismatchTest {

    @ArquillianResource
    private Deployer deployer;

    @NamedMetadata(name = "my-metadata-incorrect-2",
        metricName = "my-metric",
        type = MetricType.COUNTER)
    public static class IncorrectlyAnnotated2 {

        // named metadata declares COUNTER but this is a METER
        @Metered(metadata = "my-metadata-incorrect-2")
        public void meteredMethod() {
        }

    }

    @Deployment(name = "d2", managed = false)
    @ShouldThrowException(value = DeploymentException.class)
    public static WebArchive createDeployment2() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(IncorrectlyAnnotated2.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    public void testTypeMismatch() {
        deployer.deploy("d2");
    }

}
