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
import org.eclipse.microprofile.metrics.annotation.Gauge;
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
 * NamedMetadata for type=gauge must not contain reusable=true.
 */
@RunWith(Arquillian.class)
public class NamedMetadataReusableGaugeTest {

    @ArquillianResource
    private Deployer deployer;

    @NamedMetadata(name = "foo", type = MetricType.GAUGE, metricName = "metric1", reusable = true)
    public static class ContainsNamedMetadataWithReusableGauge {

        @Gauge(metadata = "foo")
        public void countedMethod() {
        }

    }

    @Deployment(name = "deployment", managed = false)
    @ShouldThrowException(value = DeploymentException.class)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(ContainsNamedMetadataWithReusableGauge.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testReferenceToUnknownNamedMetadata() {
        deployer.deploy("deployment");
    }

}
