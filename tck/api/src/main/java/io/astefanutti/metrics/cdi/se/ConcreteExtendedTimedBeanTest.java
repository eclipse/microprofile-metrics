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
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConcreteExtendedTimedBeanTest {

    private final static String TIMED_NAME = MetricRegistry.name(ConcreteExtendedTimedBean.class, "timedMethod");
    private final static String EXTENDED_TIMED_NAME = MetricRegistry.name(ConcreteExtendedTimedBean.class, "anotherTimedMethod");

    private static MetricID timedMID;
    private static MetricID extendedTimedMID;
    
    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // Test bean
            .addClasses(ConcreteExtendedTimedBean.class, AbstractTimedBean.class)
            // Bean archive deployment descriptor
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private ConcreteExtendedTimedBean bean;

    @Before
    public void instantiateTest() {
        /*
         * The MetricID relies on the MicroProfile Config API.
         * Running a managed arquillian container will result
         * with the MetricID being created in a client process
         * that does not contain the MPConfig impl.
         * 
         * This will cause client instantiated MetricIDs to 
         * throw an exception. (i.e the global MetricIDs)
         */
        timedMID = new MetricID(TIMED_NAME);
        extendedTimedMID = new MetricID(EXTENDED_TIMED_NAME);
    }

    
    @Test
    @InSequence(1)
    public void timedMethodNotCalledYet(MetricRegistry registry) {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timedMID));
        Timer timer = registry.getTimers().get(timedMID);

        // Make sure that the timer hasn't been called yet
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void extendedTimedMethodNotCalledYet(MetricRegistry registry) {
        assertThat("Timer is not registered correctly on the methods on the abstract class", registry.getTimers(), hasKey(extendedTimedMID));
        Timer timer = registry.getTimers().get(extendedTimedMID);

        // Make sure that the timer hasn't been called yet
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(0L)));
    }
    
    @Test
    @InSequence(3)
    public void callTimedMethodOnce(MetricRegistry registry) {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timedMID));
        Timer timer = registry.getTimers().get(timedMID);

        // Call the timed method and assert it's been timed
        bean.timedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(1L)));
    }
    
    @Test
    @InSequence(4)
    public void callExtendedTimedMethodOnce(MetricRegistry registry) {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(extendedTimedMID));
        Timer timer = registry.getTimers().get(extendedTimedMID);

        // Call the timed method and assert it's been timed
        bean.anotherTimedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(1L)));
    }
}
