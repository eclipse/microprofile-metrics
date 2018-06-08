/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi.se;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MeterInjectionBeanTest {

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClass(MeteredMethodBean2.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MeteredMethodBean2 bean;

    @Inject
    @Metric(absolute = true, name = "io.astefanutti.metrics.cdi.se.MeteredMethodBean2.meteredMethod")
    private Meter meter;

    @Test
    @InSequence(1)
    public void meteredMethodNotCalledYet() {
        // Make sure that the meter hasn't been called yet
        assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callMeteredMethodOnce() {
        // Call the metered method and assert it's been marked
        bean.meteredMethod();

        // Make sure that the meter has been called
        assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(1L)));
    }
}