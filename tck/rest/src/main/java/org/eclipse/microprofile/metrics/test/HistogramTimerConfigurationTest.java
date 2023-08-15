/*
 **********************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.metrics.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(Arquillian.class)
/**
 *
 * SEE microprofile-metrics-rest-tck/src/main/resources/META-INF/microprofile-config.properties for full config
 *
 */
public class HistogramTimerConfigurationTest {
    private static final String TEXT_PLAIN = "text/plain";

    private static final String PROM_APP_LABEL_REGEX = "mp_app=\"[-/A-Za-z0-9]+\"";

    private static final String DEFAULT_PROTOCOL = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    /*
     * Filters out _app tag plus any leading or trailing commas
     */
    private static String filterOutAppLabelPromMetrics(String responseBody) {
        return responseBody.replaceAll(PROM_APP_LABEL_REGEX, "").replaceAll("\\{,", "{").replaceAll(",\\}", "}");
    }

    @Inject
    private HistogramTimerConfigBean histogramTimerConfigBean;

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
        WebArchive jar = ShrinkWrap.create(WebArchive.class).addClass(HistogramTimerConfigBean.class)
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
                .addAsManifestResource("META-INF/microprofile-config.properties", "microprofile-config.properties");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void testAnnotatedTimerCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> annotatedTimerCustomPercentile=0.3,0.5,0.6
         */

        resp.then().statusCode(200)

                // CHECK SUMMARY
                .body(containsString(
                        "# HELP annotatedTimerCustomPercentile_seconds"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomPercentile_seconds summary"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.3\"}"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.6\"}"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP annotatedTimerCustomPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomPercentile_seconds_max gauge"))
                .body(containsString(
                        "annotatedTimerCustomPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE THAT ONLY ABOVE QUANTILES EXIST AND THAT THE DEFAULTS ARE NOT PRESENT
                .body(not(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));

    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void testAnnotatedTimerNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> annotatedTimerNoPercentile=
         */

        resp.then().statusCode(200)
                // CHECK SUMMARY
                .body(containsString(
                        "# HELP annotatedTimerNoPercentile_seconds"))
                .body(containsString(
                        "# TYPE annotatedTimerNoPercentile_seconds summary"))
                .body(containsString(
                        "annotatedTimerNoPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "annotatedTimerNoPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP annotatedTimerNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE annotatedTimerNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "annotatedTimerNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE QUANTILES ARE DISABLED
                .body(not(containsString(
                        "annotatedTimerNoPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile")));

    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void testAnnotatedTimerCustomBucketsDefaultPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * BUCKET CONFIG ->annotatedTimerCustomBucketsDefaultPercentile=100,200ms,2s,1m,1h
         */
        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP annotatedTimerCustomBucketsDefaultPercentile_seconds"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomBucketsDefaultPercentile_seconds histogram"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.1\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.2\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"2.0\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"60.0\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"3600.0\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP annotatedTimerNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE annotatedTimerNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "annotatedTimerNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"));

    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void testAnnotatedTimerCustomBucketsCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> annotatedTimerCustomBucketsCustomPercentile=0.4,0.7 BUCKET CONFIG ->
         * annotatedTimerCustomBucketsCustomPercentile=120ms,3s
         */
        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP annotatedTimerCustomBucketsCustomPercentile_seconds"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomBucketsCustomPercentile_seconds histogram"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.12\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"3.0\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.4\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.7\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP annotatedTimerCustomBucketsCustomPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomBucketsCustomPercentile_seconds_max gauge"))
                .body(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE DEFAULT QUANTILES ARE DISABLED
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "annotatedTimerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));
    }

    @Test
    @RunAsClient
    @InSequence(5)
    public void testAnnotatedTimerCustomBucketsNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> annotatedTimerCustomBucketsNoPercentile= BUCKET CONFIG ->
         * annotatedTimerCustomBucketsNoPercentile=789ms,2s
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP annotatedTimerCustomBucketsNoPercentile_seconds"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomBucketsNoPercentile_seconds histogram"))
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.789\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"2.0\"}"))
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP annotatedTimerCustomBucketsNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE annotatedTimerCustomBucketsNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE NO QUANTILES ARE OUTPUTTED
                .body(not(containsString(
                        "annotatedTimerCustomBucketsNoPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile")));
    }

    @Test
    @InSequence(6)
    public void testSetupApplicationMetrics() {
        histogramTimerConfigBean.programmaticTimers();
        histogramTimerConfigBean.programmaticHistograms();
        histogramTimerConfigBean.programmaticBadConfigs();
        histogramTimerConfigBean.precedence();
    }

    @Test
    @RunAsClient
    @InSequence(7)
    public void testTimerCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> timerCustomPercentile=0.3,0.5,0.6
         */

        resp.then().statusCode(200)

                // CHECK SUMMARY
                .body(containsString(
                        "# HELP timerCustomPercentile_seconds"))
                .body(containsString(
                        "# TYPE timerCustomPercentile_seconds summary"))
                .body(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.3\"}"))
                .body(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.6\"}"))
                .body(containsString(
                        "timerCustomPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerCustomPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerCustomPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE timerCustomPercentile_seconds_max gauge"))
                .body(containsString(
                        "timerCustomPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE THAT ONLY ABOVE QUANTILES EXIST AND THAT THE DEFAULTS ARE NOT PRESENT
                .body(not(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "timerCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));

    }

    @Test
    @RunAsClient
    @InSequence(8)
    public void testTimerNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> timerNoPercentile=
         */

        resp.then().statusCode(200)
                // CHECK SUMMARY
                .body(containsString(
                        "# HELP timerNoPercentile_seconds"))
                .body(containsString(
                        "# TYPE timerNoPercentile_seconds summary"))
                .body(containsString(
                        "timerNoPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerNoPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE timerNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "timerNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE QUANTILES ARE DISABLED
                .body(not(containsString(
                        "timerNoPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile")));

    }

    @Test
    @RunAsClient
    @InSequence(9)
    public void testTimerCustomBucketsDefaultPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * BUCKET CONFIG -> timerCustomBucketsDefaultPercentile=100,200ms,2s,1m,1h
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP timerCustomBucketsDefaultPercentile_seconds"))
                .body(containsString(
                        "# TYPE timerCustomBucketsDefaultPercentile_seconds histogram"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.1\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.2\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"2.0\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"60.0\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"3600.0\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "timerCustomBucketsDefaultPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE timerNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "timerNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"));

    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testTimerCustomBucketsCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> timerCustomBucketsCustomPercentile=0.4,0.7 BUCKET CONFIG ->
         * timerCustomBucketsCustomPercentile=120ms,3s
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP timerCustomBucketsCustomPercentile_seconds"))
                .body(containsString(
                        "# TYPE timerCustomBucketsCustomPercentile_seconds histogram"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.12\"}"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"3.0\"}"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.4\"}"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.7\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerCustomBucketsCustomPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE timerCustomBucketsCustomPercentile_seconds_max gauge"))
                .body(containsString(
                        "timerCustomBucketsCustomPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE DEFAULT QUANTILES ARE DISABLED
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}")))
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "timerCustomBucketsCustomPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));
    }

    @Test
    @RunAsClient
    @InSequence(11)
    public void testTimerCustomBucketsNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> timerCustomBucketsNoPercentile= BUCKET CONFIG -> timerCustomBucketsNoPercentile=789ms,2s
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP timerCustomBucketsNoPercentile_seconds"))
                .body(containsString(
                        "# TYPE timerCustomBucketsNoPercentile_seconds histogram"))
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.789\"}"))
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"2.0\"}"))
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerCustomBucketsNoPercentile_seconds_max"))
                .body(containsString(
                        "# TYPE timerCustomBucketsNoPercentile_seconds_max gauge"))
                .body(containsString(
                        "timerCustomBucketsNoPercentile_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE NO QUANTILES ARE OUTPUTTED
                .body(not(containsString(
                        "timerCustomBucketsNoPercentile_seconds{mp_scope=\"application\",tier=\"integration\",quantile")));
    }

    @Test
    @RunAsClient
    @InSequence(12)
    public void testHistogramCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> histogramCustomPercentile=0.3,0.5,0.6
         *
         */

        resp.then().statusCode(200)

                // CHECK SUMMARY
                .body(containsString(
                        "# HELP histogramCustomPercentile"))
                .body(containsString(
                        "# TYPE histogramCustomPercentile summary"))
                .body(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.3\"}"))
                .body(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.6\"}"))
                .body(containsString(
                        "histogramCustomPercentile_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramCustomPercentile_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramCustomPercentile_max"))
                .body(containsString(
                        "# TYPE histogramCustomPercentile_max gauge"))
                .body(containsString(
                        "histogramCustomPercentile_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE THAT ONLY ABOVE QUANTILES EXIST AND THAT THE DEFAULTS ARE NOT PRESENT
                .body(not(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "histogramCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));

    }

    @Test
    @RunAsClient
    @InSequence(13)
    public void testHistogramNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> histogramNoPercentile=
         */

        resp.then().statusCode(200)
                // CHECK SUMMARY
                .body(containsString(
                        "# HELP histogramNoPercentile"))
                .body(containsString(
                        "# TYPE histogramNoPercentile summary"))
                .body(containsString(
                        "histogramNoPercentile_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramNoPercentile_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramNoPercentile_max"))
                .body(containsString(
                        "# TYPE histogramNoPercentile_max gauge"))
                .body(containsString(
                        "histogramNoPercentile_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE QUANTILES ARE DISABLED
                .body(not(containsString(
                        "histogramNoPercentile{mp_scope=\"application\",tier=\"integration\",quantile")));

    }

    @Test
    @RunAsClient
    @InSequence(14)
    public void testHistogramCustomBucketsDefaultPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * BUCKET CONFIG -> histogramCustomBucketsDefaultPercentile=100,200,345
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP histogramCustomBucketsDefaultPercentile"))
                .body(containsString(
                        "# TYPE histogramCustomBucketsDefaultPercentile histogram"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"100.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"200.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"345.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "histogramCustomBucketsDefaultPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramNoPercentile_max"))
                .body(containsString(
                        "# TYPE histogramNoPercentile_max gauge"))
                .body(containsString(
                        "histogramNoPercentile_max{mp_scope=\"application\",tier=\"integration\"}"));

    }

    @Test
    @RunAsClient
    @InSequence(15)
    public void testHistogramCustomBucketsCustomPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> histogramCustomBucketsCustomPercentile=0.4,0.7 BUCKET CONFIG ->
         * histogramCustomBucketsCustomPercentile=120,3
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP histogramCustomBucketsCustomPercentile"))
                .body(containsString(
                        "# TYPE histogramCustomBucketsCustomPercentile histogram"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"3.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"120.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // QUANTILES
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.4\"}"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.7\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramCustomBucketsCustomPercentile_max"))
                .body(containsString(
                        "# TYPE histogramCustomBucketsCustomPercentile_max gauge"))
                .body(containsString(
                        "histogramCustomBucketsCustomPercentile_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE DEFAULT QUANTILES ARE DISABLED
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}")))
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "histogramCustomBucketsCustomPercentile{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));
    }

    @Test
    @RunAsClient
    @InSequence(16)
    public void testHistogramCustomBucketsNoPercentile() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> histogramCustomBucketsNoPercentile= BUCKET CONFIG ->
         * histogramCustomBucketsNoPercentile=789,67
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP histogramCustomBucketsNoPercentile"))
                .body(containsString(
                        "# TYPE histogramCustomBucketsNoPercentile histogram"))
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"67.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"789.0\"}"))
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramCustomBucketsNoPercentile_max"))
                .body(containsString(
                        "# TYPE histogramCustomBucketsNoPercentile_max gauge"))
                .body(containsString(
                        "histogramCustomBucketsNoPercentile_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE NO QUANTILES ARE OUTPUTTED
                .body(not(containsString(
                        "histogramCustomBucketsNoPercentile{mp_scope=\"application\",tier=\"integration\",quantile")));
    }

    @Test
    @RunAsClient
    @InSequence(17)
    public void testTimerBadPercentiles() {

        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> timerBadPercentiles=()sdf,0.34fdsf,0.1,(//2,f0.3,33,1.1,><
         */

        resp.then().statusCode(200)

                // CHECK SUMMARY
                .body(containsString(
                        "# HELP timerBadPercentiles_seconds"))
                .body(containsString(
                        "# TYPE timerBadPercentiles_seconds summary"))
                .body(containsString(
                        "timerBadPercentiles_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerBadPercentiles_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.1\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerBadPercentiles_seconds_max"))
                .body(containsString(
                        "# TYPE timerBadPercentiles_seconds_max gauge"))
                .body(containsString(
                        "timerBadPercentiles_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE DEFAULT QUANTILES ARE DISABLED
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}")))
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "timerBadPercentiles_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));
    }

    @Test
    @RunAsClient
    @InSequence(18)
    public void testHistogramBadPercentiles() {

        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> histogramBadPercentiles=0.1,adf,fd.ks,0.3,%0.4,()*&$(,0.4,/0.1
         */

        resp.then().statusCode(200)

                // CHECK SUMMARY
                .body(containsString(
                        "# HELP histogramBadPercentiles"))
                .body(containsString(
                        "# TYPE histogramBadPercentiles summary"))
                .body(containsString(
                        "histogramBadPercentiles_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramBadPercentiles_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.1\"}"))
                .body(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.3\"}"))
                .body(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.4\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramBadPercentiles_max"))
                .body(containsString(
                        "# TYPE histogramBadPercentiles_max gauge"))
                .body(containsString(
                        "histogramBadPercentiles_max{mp_scope=\"application\",tier=\"integration\"}"))

                // ENSURE DEFAULT QUANTILES ARE DISABLED
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}")))
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}")))
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}")))
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}")))
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}")))
                .body(not(containsString(
                        "histogramBadPercentiles{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}")));
    }

    @Test
    @RunAsClient
    @InSequence(19)
    public void testTimerBadBuckets() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * BUCKET CONFIG -> timerBadBuckets=sdf,10,30ms,500,sdf.s,90msh,12.0,&*(,//sdf,,90dk,,.,
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP timerBadBuckets_seconds"))
                .body(containsString(
                        "# TYPE timerBadBuckets_seconds histogram"))
                .body(containsString(
                        "timerBadBuckets_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "timerBadBuckets_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.01\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.03\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.5\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP timerBadBuckets_seconds_max"))
                .body(containsString(
                        "# TYPE timerBadBuckets_seconds_max gauge"))
                .body(containsString(
                        "timerBadBuckets_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                // DEFAULT QUANTILES
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "timerBadBuckets_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(20)
    public void testHistogramBadBuckets() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * BUCKET CONFIG -> histogramBadBuckets=sdf,10,sdf.s,12.0,&*(,//sdf,,90
         */

        resp.then().statusCode(200)

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP histogramBadBuckets"))
                .body(containsString(
                        "# TYPE histogramBadBuckets histogram"))
                .body(containsString(
                        "histogramBadBuckets_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "histogramBadBuckets_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // BUCKETS
                .body(containsString(
                        "histogramBadBuckets_bucket{mp_scope=\"application\",tier=\"integration\",le=\"10.0\"}"))
                .body(containsString(
                        "histogramBadBuckets_bucket{mp_scope=\"application\",tier=\"integration\",le=\"12.0\"}"))
                .body(containsString(
                        "histogramBadBuckets_bucket{mp_scope=\"application\",tier=\"integration\",le=\"90.0\"}"))
                .body(containsString(
                        "histogramBadBuckets_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP histogramBadBuckets_max"))
                .body(containsString(
                        "# TYPE histogramBadBuckets_max gauge"))
                .body(containsString(
                        "histogramBadBuckets_max{mp_scope=\"application\",tier=\"integration\"}"))

                // DEFAULT QUANTILES
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "histogramBadBuckets{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(21)
    public void testHistogramPrecedence() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> precedence.*=0.8,0.9 BUCKET CONFIG -> precedence.*=23,45 PERCENTILE CONFIG ->
         * precedence.override.histogram=0.2 BUCKET CONFIG -> precedence.override.histogram=32
         */

        resp.then().statusCode(200)

                /*
                 * CHECK precedence.histogram
                 */

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP precedence_histogram"))
                .body(containsString(
                        "# TYPE precedence_histogram histogram"))
                .body(containsString(
                        "precedence_histogram_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "precedence_histogram_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "precedence_histogram{mp_scope=\"application\",tier=\"integration\",quantile=\"0.8\"}"))
                .body(containsString(
                        "precedence_histogram{mp_scope=\"application\",tier=\"integration\",quantile=\"0.9\"}"))

                // BUCKETS
                .body(containsString(
                        "precedence_histogram_bucket{mp_scope=\"application\",tier=\"integration\",le=\"23.0\"}"))
                .body(containsString(
                        "precedence_histogram_bucket{mp_scope=\"application\",tier=\"integration\",le=\"45.0\"}"))
                .body(containsString(
                        "precedence_histogram_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP precedence_histogram_max"))
                .body(containsString(
                        "# TYPE precedence_histogram_max gauge"))
                .body(containsString(
                        "precedence_histogram_max{mp_scope=\"application\",tier=\"integration\"}"))

                /*
                 * CHECK precedence.override.histogram
                 */

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP precedence_override_histogram"))
                .body(containsString(
                        "# TYPE precedence_override_histogram histogram"))
                .body(containsString(
                        "precedence_override_histogram_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "precedence_override_histogram_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "precedence_override_histogram{mp_scope=\"application\",tier=\"integration\",quantile=\"0.2\"}"))

                // BUCKETS
                .body(containsString(
                        "precedence_override_histogram_bucket{mp_scope=\"application\",tier=\"integration\",le=\"32.0\"}"))
                .body(containsString(
                        "precedence_override_histogram_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP precedence_override_histogram_max"))
                .body(containsString(
                        "# TYPE precedence_override_histogram_max gauge"))
                .body(containsString(
                        "precedence_override_histogram_max{mp_scope=\"application\",tier=\"integration\"}"));

    }

    @Test
    @RunAsClient
    @InSequence(22)
    public void testTimerPrecedence() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(acceptHeader).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * PERCENTILE CONFIG -> precedence.*=0.8,0.9 BUCKET CONFIG -> precedence.*=23ms,455ms BUCKET CONFIG ->
         * precedence.override.timer=32s
         */

        resp.then().statusCode(200)

                /*
                 * CHECK precedence.histogram
                 */

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP precedence_timer_seconds"))
                .body(containsString(
                        "# TYPE precedence_timer_seconds histogram"))
                .body(containsString(
                        "precedence_timer_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "precedence_timer_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "precedence_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.8\"}"))
                .body(containsString(
                        "precedence_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.9\"}"))

                // BUCKETS
                .body(containsString(
                        "precedence_timer_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.023\"}"))
                .body(containsString(
                        "precedence_timer_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"0.455\"}"))
                .body(containsString(
                        "precedence_timer_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP precedence_timer_seconds_max"))
                .body(containsString(
                        "# TYPE precedence_timer_seconds_max gauge"))
                .body(containsString(
                        "precedence_timer_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                /*
                 * CHECK precedence.override.timer
                 */

                // CHECK HISTOGRAM
                .body(containsString(
                        "# HELP precedence_override_timer_seconds"))
                .body(containsString(
                        "# TYPE precedence_override_timer_seconds histogram"))
                .body(containsString(
                        "precedence_override_timer_seconds_count{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString(
                        "precedence_override_timer_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))

                // QUANTILES
                .body(containsString(
                        "precedence_override_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.3\"}"))

                // BUCKETS
                .body(containsString(
                        "precedence_override_timer_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"32.0\"}"))
                .body(containsString(
                        "precedence_override_timer_seconds_bucket{mp_scope=\"application\",tier=\"integration\",le=\"+Inf\"}"))

                // Check MAX
                .body(containsString(
                        "# HELP precedence_override_timer_seconds_max"))
                .body(containsString(
                        "# TYPE precedence_override_timer_seconds_max gauge"))
                .body(containsString(
                        "precedence_override_timer_seconds_max{mp_scope=\"application\",tier=\"integration\"}"));

    }
}
