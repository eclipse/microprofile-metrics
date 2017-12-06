/*
 * ********************************************************************
 *  Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 *  See the NOTICES file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ********************************************************************
 *
 */

package org.eclipse.microprofile.metrics.test;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Header;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Heiko W. Rupp
 */
@RunWith(Arquillian.class)
public class MicroprofileMetricsTest2 {

  private static final String APPLICATION_JSON = "application/json";

  @Inject
  private MetricAppBean2 metricAppBean;


  @Deployment
  public static JavaArchive createDeployment() {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClass(MetricAppBean2.class)
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      return jar;
  }


  @Test
  @InSequence(1)
  public void setA() {
    metricAppBean.countMeA();
    metricAppBean.meterMeA();
    metricAppBean.timeMeA();
  }

  @Test
  @RunAsClient
  @InSequence(2)
  public void testSharedCounter() {

    Header acceptJson = new Header("Accept", APPLICATION_JSON);

    JsonPath path = given().header(acceptJson).get("/metrics/application").jsonPath();

    assert path.getInt("countMe2") == 1;
    assert path.getInt("'org.eclipse.microprofile.metrics.test.MetricAppBean2.meterMe2'.count") == 1;
    assert path.getInt("timeMe2.count") == 1;


  }

  @Test
  @InSequence(3)
  public void setB() {
    metricAppBean.countMeB();
    metricAppBean.meterMeB();
    metricAppBean.timeMeB();
  }

  @Test
  @RunAsClient
  @InSequence(4)
  public void testSharedCounterAgain() {

    Header acceptJson = new Header("Accept", APPLICATION_JSON);

    JsonPath path = given().header(acceptJson).get("/metrics/application").jsonPath();

    assert path.getInt("countMe2") == 2;
    assert path.getInt("'org.eclipse.microprofile.metrics.test.MetricAppBean2.meterMe2'.count") == 2;
    assert path.getInt("timeMe2.count") == 2;

  }

  @Test
  @InSequence(5)
  public void setReusableHistogram() {
    metricAppBean.registerReusableHistogram();
  }


  @Test
  @RunAsClient
  @InSequence(6)
  public void testReusedHistogram() {

    Header acceptJson = new Header("Accept", APPLICATION_JSON);

    JsonPath path = given().header(acceptJson).get("/metrics/application").jsonPath();

    assert path.getInt("reusableHisto.count") == 2;
    assert path.getInt("reusableHisto.min") == 1;
    assert path.getInt("reusableHisto.max") == 3;

  }

  @Test
  @InSequence(7)
  public void testBadReusable() {

    try {
      metricAppBean.badRegisterReusableHistogram();
    }
    catch (IllegalArgumentException e) {
      return; // This was expected
    }
    assert false;
  }

  @Test
  @InSequence(8)
  public void testBadReusable2() {

    try {
      metricAppBean.badRegisterReusableHistogram2();
    }
    catch (IllegalArgumentException e) {
      return; // This was expected
    }
    assert false;
  }

  @Test
  @InSequence(9)
  public void testBadReusableMixed() {

    try {
      metricAppBean.badRegisterReusableMixed();
    }
    catch (IllegalArgumentException e) {
      return; // This was expected
    }
    assert false;
  }


}
