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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author Heiko W. Rupp
 */
@RunWith(Arquillian.class)
public class ReusableMetricsTest {

  private static final String JSON_APP_LABEL_REGEX = ";_app=[-/A-Za-z0-9]+([;\\\"]?)"; 
  private static final String JSON_APP_LABEL_REGEXS_SUB = "$1";   
  
  private static final String APPLICATION_JSON = "application/json";
  private static final String DEFAULT_PROTOCOL = "http";
  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;

  @Inject
    private MetricAppBean2 metricAppBean;

    @BeforeClass
    static public void setup() throws MalformedURLException {
        // set base URI and port number to use for all requests
        String serverUrl = System.getProperty("test.url");
        String protocol = DEFAULT_PROTOCOL;
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (serverUrl != null) {
            URL url = new URL(serverUrl);
            protocol = url.getProtocol();
            host = url.getHost();
            port = (url.getPort() == -1) ? DEFAULT_PORT : url.getPort();
        }

        RestAssured.baseURI = protocol + "://" + host;
        RestAssured.port = port;

        // set user name and password to use for basic authentication for all requests
        String userName = System.getProperty("test.user");
        String password = System.getProperty("test.pwd");

        if (userName != null && password != null) {
            RestAssured.authentication = RestAssured.basic(userName, password);
            RestAssured.useRelaxedHTTPSValidation();
        }

    }
    
  @Deployment
  public static WebArchive createDeployment() {
      WebArchive jar = ShrinkWrap.create(WebArchive.class).addClass(MetricAppBean2.class)
              .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

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
        
        Response resp = given().header(acceptJson).get("/metrics/application");
        JsonPath filteredJSONPath = new JsonPath(resp.jsonPath().prettify().replaceAll(JSON_APP_LABEL_REGEX, JSON_APP_LABEL_REGEXS_SUB));
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filteredJSONPath.prettify());
        resp = responseBuilder.build();
        
        resp.then()
                .assertThat().body("'countMe2;tier=integration'", equalTo(1))
                .assertThat().body("'org.eclipse.microprofile.metrics.test.MetricAppBean2.meterMe2'.'count;tier=integration'", equalTo(1))
                .assertThat().body("'timeMe2'.'count;tier=integration'", equalTo(1));

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

    Response resp = given().header(acceptJson).get("/metrics/application");
    JsonPath filteredJSONPath = new JsonPath(resp.jsonPath().prettify().replaceAll(JSON_APP_LABEL_REGEX, JSON_APP_LABEL_REGEXS_SUB));
    ResponseBuilder responseBuilder = new ResponseBuilder();
    responseBuilder.clone(resp);
    responseBuilder.setBody(filteredJSONPath.prettify());
    resp = responseBuilder.build();
    
    resp.then()
    .assertThat().body("'countMe2;tier=integration'", equalTo(2))
    .assertThat().body("'org.eclipse.microprofile.metrics.test.MetricAppBean2.meterMe2'.'count;tier=integration'", equalTo(2))
    .assertThat().body("'timeMe2'.'count;tier=integration'", equalTo(2));
    

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

    Response resp = given().header(acceptJson).get("/metrics/application");
    JsonPath filteredJSONPath = new JsonPath(resp.jsonPath().prettify().replaceAll(JSON_APP_LABEL_REGEX, JSON_APP_LABEL_REGEXS_SUB));
    ResponseBuilder responseBuilder = new ResponseBuilder();
    responseBuilder.clone(resp);
    responseBuilder.setBody(filteredJSONPath.prettify());
    resp = responseBuilder.build();
    
    resp.then()
    .assertThat().body("'reusableHisto'.'count;tier=integration'", equalTo(2))
    .assertThat().body("'reusableHisto'.'min;tier=integration'", equalTo(1))
    .assertThat().body("'reusableHisto'.'max;tier=integration'", equalTo(3));

  }

  @Test(expected=IllegalArgumentException.class)
  @InSequence(7)
  public void testBadReusableMixed() {
      metricAppBean.badRegisterReusableMixed();
  }


}
