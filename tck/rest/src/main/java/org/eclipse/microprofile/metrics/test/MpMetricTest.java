/*
 **********************************************************************
 * Copyright (c) 2017, 2022 Contributors to the Eclipse Foundation
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
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.inject.Inject;

/**
 * Rest Test Kit
 *
 * @author Heiko W. Rupp <hrupp@redhat.com>
 * @author Don Bourne <dbourne@ca.ibm.com>
 */
@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(Arquillian.class)
public class MpMetricTest {
    private static final String TEXT_PLAIN = "text/plain";

    private static final String PROM_APP_LABEL_REGEX = "mp_app=\"[-/A-Za-z0-9]+\"";

    private static final String DEFAULT_PROTOCOL = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static final double TOLERANCE = 0.025;

    /*
     * Filters out _app tag plus any leading or trailing commas
     */
    private static String filterOutAppLabelPromMetrics(String responseBody) {
        return responseBody.replaceAll(PROM_APP_LABEL_REGEX, "").replaceAll("\\{,", "{").replaceAll(",\\}", "}");
    }

    @Inject
    private MetricAppBean metricAppBean;

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
        WebArchive jar = ShrinkWrap.create(WebArchive.class).addClass(MetricAppBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void testTextPlainResponseContentType() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        given().header(acceptHeader).when().get("/metrics").then().statusCode(200).and().contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void testRequestPathReturn404() {
        when().get("/metrics/bad-tree").then().statusCode(404);
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void testBadScopeReturn404() {
        when().get("/metrics?scope=fakescope").then().statusCode(404);
    }

    @Test
    @RunAsClient
    @InSequence(6)
    public void testBasePromMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Response resp = given().header("Accept", TEXT_PLAIN).get("/metrics?scope=base");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();
        resp.then().statusCode(200).and().contentType(TEXT_PLAIN).and()
                .body(containsString("# TYPE thread_max_count"))
                .body(containsString("thread_max_count{mp_scope=\"base\",tier=\"integration\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(9)
    public void testBaseAttributePromMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Response resp = given().header("Accept", TEXT_PLAIN).get("/metrics?scope=base&name=thread.max.count");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();
        resp.then().statusCode(200).and()
                .contentType(TEXT_PLAIN).and().body(containsString("# TYPE thread_max_count"),
                        containsString("thread_max_count{mp_scope=\"base\",tier=\"integration\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(13)
    public void testPromMetricsFormatNoBadChars() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);

        String data = given().header(wantPromMetricsFormat).get("/metrics?scope=base").asString();

        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            String nameAndTagsPart = line.substring(0, line.lastIndexOf(" "));
            String namePart = nameAndTagsPart.contains("{")
                    ? nameAndTagsPart.substring(0, nameAndTagsPart.lastIndexOf("{"))
                    : nameAndTagsPart;
            assertFalse("Name has illegal chars " + line, namePart.matches(".*[-.].*"));
            assertFalse("Found __ in " + line, line.matches(".*__.*"));
        }
    }

    /*
     * Technically PromMetrics has no metadata call and this is included inline in the response.
     */
    @Test
    @RunAsClient
    @InSequence(14)
    public void testBaseMetadataSingluarItemsPromMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);

        String data = given().header(wantPromMetricsFormat).get("/metrics?scope=base").asString();

        String[] lines = data.split("\n");

        Map<String, MiniMeta> expectedMetadata = getExpectedMetadataFromXmlFile(MetricRegistry.BASE_SCOPE);
        for (MiniMeta mm : expectedMetadata.values()) {

            boolean found = false;
            // Skip GC and optional metrics
            if (mm.name.startsWith("gc.") || expectedMetadata.get(mm.name).optional) {
                continue;
            }
            for (String line : lines) {

                // Find only lines with TYPE
                if (!line.startsWith("# TYPE")) {
                    continue;
                }

                String promName = mm.toPromString();

                // Expect [#,TYPE,<name>,<type>]
                String[] tmp = line.split(" ");
                assertEquals("Bad entry: " + line, tmp.length, 4);

                if (tmp[2].startsWith(promName)) {
                    found = true;
                    assertEquals("Expected [" + mm.toString() + "] got [" + line + "]", tmp[3], mm.type);
                }
            }
            assertTrue("Not found [" + mm.toString() + "]", found);

        }
    }

    @Test
    @InSequence(17)
    public void testSetupApplicationMetrics() {

        metricAppBean.countMe();
        metricAppBean.countMeA();
        metricAppBean.countMeB();

        metricAppBean.gaugeMe();
        metricAppBean.gaugeMeA();
        metricAppBean.gaugeMeB();
        metricAppBean.gaugeMeTagged();
        metricAppBean.gaugeMeTaggedOne();
        metricAppBean.gaugeMeTaggedTwo();

        metricAppBean.histogramMe();

        metricAppBean.timeMe();
        metricAppBean.timeMeA();

    }

    /*
     * WILL TEST FOR TYPE, HELP, VALUE LINES This in effect tests for "metadata" as well
     */
    @Test
    @RunAsClient
    @InSequence(18)
    public void testApplicationMetricsPrometheus() {
        Header wantPrometheus = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(wantPrometheus).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        resp.then().statusCode(200)

                /*
                 * COUNTERS
                 */
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_redCount_total counter"))
                .body(containsString(
                        "# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_redCount_total red-description"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_redCount_total{mp_scope=\"application\",tier=\"integration\"} 0"))

                .body(containsString("# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_blue_total counter"))
                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_blue_total"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_blue_total{mp_scope=\"application\",tier=\"integration\"} 0"))

                .body(containsString("# TYPE greenCount_total counter"))
                .body(containsString("# HELP greenCount_total"))
                .body(containsString(
                        "greenCount_total{mp_scope=\"application\",tier=\"integration\"} 0"))

                .body(containsString("# TYPE purple_total counter"))
                .body(containsString("# HELP purple_total"))
                .body(containsString(
                        "purple_total{app=\"myShop\",mp_scope=\"application\",tier=\"integration\"} 0"))

                .body(containsString("# HELP metricTest_test1_count_total"))
                .body(containsString("# TYPE metricTest_test1_count_total counter"))
                .body(containsString(
                        "metricTest_test1_count_total{mp_scope=\"application\",tier=\"integration\"} 1"))

                .body(containsString("# HELP metricTest_test1_countMeA_total count-me-a-description"))
                .body(containsString("# TYPE metricTest_test1_countMeA_total counter"))
                .body(containsString(
                        "metricTest_test1_countMeA_total{mp_scope=\"application\",tier=\"integration\"} 1"))

                .body(containsString("# HELP metricTest_test1_countMeB_jellybean_total"))
                .body(containsString("# TYPE metricTest_test1_countMeB_jellybean_total counter"))
                .body(containsString(
                        "metricTest_test1_countMeB_jellybean_total{mp_scope=\"application\",tier=\"integration\"} 1"))

                /*
                 * GAUGES
                 */

                .body(containsString("# HELP metricTest_test1_gauge_gigabytes"))
                .body(containsString("# TYPE metricTest_test1_gauge_gigabytes gauge"))
                .body(containsString(
                        "metricTest_test1_gauge_gigabytes{mp_scope=\"application\",tier=\"integration\"} 19"))

                .body(containsString(
                        "# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeA_kibibits gauge-me-a-description"))
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeA_kibibits gauge"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeA_kibibits{mp_scope=\"application\",tier=\"integration\"} 1000"))

                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeB_hands"))
                .body(containsString("# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeB_hands gauge"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeB_hands{mp_scope=\"application\",tier=\"integration\"} 7777777"))

                /*
                 * HISTOGRAMS
                 * 
                 * CANNOT GURANTEE ACCURACY OF QUANTILES - WILL OMIT BETTER TESTING FOR "VALUES" WILL BE DONE IN API TCK
                 */
                .body(containsString("# HELP metricTest_test1_histogram_bytes"))
                .body(containsString("# TYPE metricTest_test1_histogram_bytes summary"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes_count{mp_scope=\"application\",tier=\"integration\"} 1000"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes_sum{mp_scope=\"application\",tier=\"integration\"} 499500"))
                .body(containsString("# HELP metricTest_test1_histogram_bytes_max"))
                .body(containsString("# TYPE metricTest_test1_histogram_bytes_max gauge"))
                .body(containsString(
                        "metricTest_test1_histogram_bytes_max{mp_scope=\"application\",tier=\"integration\"} 999"))

                /*
                 * TIMERS
                 * 
                 * ONLY INTERESTED IN THE COUNT. NOT INTERESTED IN ANY OTHER VALUES. TIMES RECORDED COULD VARY. BETTER
                 * TESTING FOR "VALUES" WILL BE DONE IN API TCKS
                 */
                .body(containsString("# HELP metricTest_test1_timer_seconds"))
                .body(containsString("# TYPE metricTest_test1_timer_seconds summary"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString(
                        "metricTest_test1_timer_seconds_count{mp_scope=\"application\",tier=\"integration\"} 1"))
                .body(containsString(
                        "metricTest_test1_timer_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString("# HELP metricTest_test1_timer_seconds_max"))
                .body(containsString("# TYPE metricTest_test1_timer_seconds_max gauge"))
                .body(containsString(
                        "metricTest_test1_timer_seconds_max{mp_scope=\"application\",tier=\"integration\"}"))

                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds"))
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds summary"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds_count{mp_scope=\"application\",tier=\"integration\"} 1"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds_sum{mp_scope=\"application\",tier=\"integration\"}"))
                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds_max"))
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds_max gauge"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_seconds_max{mp_scope=\"application\",tier=\"integration\"}"));

    }

    @Test
    @RunAsClient
    @InSequence(19)
    public void testMetricNameAcrossScopes() {

        Response resp = given().header("Accept", TEXT_PLAIN).get("/metrics?name=sharedMetricName");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * Just checks that the the TYPE line for the 4 expected metrics are there
         */
        resp.then().statusCode(200)
                // counter
                .body(containsString("# TYPE sharedMetricName_total counter"))
                // timer
                .body(containsString("# TYPE sharedMetricName_seconds_max gauge"))
                .body(containsString("# TYPE sharedMetricName_seconds summary"))
                // gauge
                .body(containsString("# TYPE sharedMetricName_jelly gauge"))
                // histogram
                .body(containsString("# TYPE sharedMetricName_marshmallow_max gauge"))
                .body(containsString("# TYPE sharedMetricName_marshmallow summary"));
    }

    @Test
    @RunAsClient
    @InSequence(22)
    public void testApplicationTagPromMetrics() {

        given().header("Accept", TEXT_PLAIN).when().get("/metrics?scope=application&name=purple")
                .then().statusCode(200)
                .and()
                .body(containsString("tier=\"integration\""))
                .body(containsString("app=\"myShop\""));
    }

    @Test
    @RunAsClient
    @InSequence(24)
    public void testApplicationTimerUnitPromMetrics() {

        String prefix = "org_eclipse_microprofile_metrics_test_MetricAppBean_timeMeA_";

        Response resp = given().header("Accept", TEXT_PLAIN)
                .get("/metrics?scope=application&name=org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        resp.then().statusCode(200)
                .and()
                .body(containsString("# TYPE " + prefix + "seconds summary"))
                .body(containsString(prefix + "seconds_count"))
                .body(containsString(prefix + "seconds_sum"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        prefix + "seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString("# TYPE " + prefix + "seconds_max gauge"))
                .body(containsString(prefix + "seconds_max"));

    }

    @Test
    @RunAsClient
    @InSequence(25)
    public void testApplicationHistogramUnitBytesPromMetrics() {

        String prefix = "metricTest_test1_histogram_";

        Response resp =
                given().header("Accept", TEXT_PLAIN).get("/metrics?scope=application&name=metricTest.test1.histogram");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        resp.then().statusCode(200)
                .and()
                .body(containsString("# TYPE " + prefix + "bytes summary"))
                .body(containsString(prefix + "bytes_count"))
                .body(containsString(prefix + "bytes_sum"))
                .body(containsString(prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        prefix + "bytes{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString("# TYPE " + prefix + "bytes_max gauge"))
                .body(containsString(prefix + "bytes_max"));
    }

    @Test
    @RunAsClient
    @InSequence(26)
    public void testApplicationHistogramUnitNonePromMetrics() {

        String prefix = "metricTest_test1_histogram2";

        Response resp =
                given().header("Accept", TEXT_PLAIN).get("/metrics?scope=application&name=metricTest.test1.histogram2");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        resp.then().statusCode(200)
                .and()
                .body(containsString("# TYPE " + prefix + " summary"))
                .body(containsString(prefix + "_count"))
                .body(containsString(prefix + "_sum"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(prefix + "{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString("# TYPE " + prefix + "_max gauge"))
                .body(containsString(prefix + "_max"));
    }

    @Test
    @RunAsClient
    @InSequence(27)
    public void testPromMetrics405NotGET() {
        given()
                .header("Accept", TEXT_PLAIN)
                .when()
                .options("/metrics/application/metricTest.test1.histogram2")
                .then()
                .statusCode(405);
    }

    @Test
    @RunAsClient
    @InSequence(30)
    public void testNonStandardUnitsPromMetrics() {

        String prefix = "jellybeanHistogram_";
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(wantPromMetricsFormat).get("/metrics?scope=application&name=jellybeanHistogram");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        resp.then().statusCode(200)
                .and()
                .body(containsString("# TYPE " + prefix + "jellybeans summary"))
                .body(containsString(prefix + "jellybeans_count"))
                .body(containsString(prefix + "jellybeans_sum"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"}"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"}"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"}"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"}"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"}"))
                .body(containsString(
                        prefix + "jellybeans{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"}"))
                .body(containsString("# TYPE " + prefix + "jellybeans_max gauge"))
                .body(containsString(prefix + "jellybeans_max"));
    }

    @Test
    @RunAsClient
    @InSequence(31)
    public void testOptionalBaseMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);

        String data = given().header(wantPromMetricsFormat).get("/metrics?scope=base").asString();

        String[] lines = data.split("\n");
        Map<String, MiniMeta> names = getExpectedMetadataFromXmlFile(MetricRegistry.BASE_SCOPE);

        for (String line : lines) {
            for (MiniMeta item : names.values()) {
                /*
                 * implicity the following line checks that the "unit" is present for the "found" optional metric. The
                 * toPromString() generates the metric name with the unit and any applicable suffixes.
                 */
                if (line.contains("# TYPE " + item.toPromString()) && names.get(item.name).optional) {
                    // now check it is the right type
                    assertThat("Wrong metric type. Should be " + item.type, line, containsString(item.type));
                }
            }
        }
    }

    @Test
    @InSequence(33)
    public void testSetupPromNoBadCharsInNames() {
        metricAppBean.createPromMetrics();
    }

    @Test
    @RunAsClient
    @InSequence(34)
    public void testPromNoBadCharsInNames() {
        given().header("Accept", TEXT_PLAIN).when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                // for visual comparison: "pm_counter-with-dashes"s
                .body(containsString("pm_counter_with_dashes"))
                // for visual comparison: "pm_counter#hash_x'y_"
                .body(containsString("pm_counter_hash_x_y_"))
                // for visual comparison: "pm_counter-umlaut-äöü"
                .body(containsString("pm_counter_umlaut_"))
                // for visual comparison: "pm_counter+accent_ê_"
                .body(containsString("pm_counter_accent_"));
    }

    @Test
    @RunAsClient
    @InSequence(35)
    public void testAccept1() {
        given().header("Accept", "application/json;q=0.5,text/plain;q=0.5")
                .when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                .contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(36)
    public void testAccept2() {
        given().header("Accept", "application/json;q=0.1,text/plain;q=0.9")
                .when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                .contentType(TEXT_PLAIN);

    }

    @Test
    @RunAsClient
    @InSequence(37)
    public void testAccept3() {
        given().header("Accept", "image/png,image/jpeg")
                .when().get("/metrics?scope=application")
                .then().statusCode(406);
    }

    @Test
    @RunAsClient
    @InSequence(38)
    public void testAccept4() {
        given().header("Accept", "*/*")
                .when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                .contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(39)
    public void testAccept5() {
        given().header("Accept", "image/png;q=1,*/*;q=0.1")
                .when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                .contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(40)
    public void testNoAcceptHeader() {
        when().get("/metrics?scope=application")
                .then().statusCode(200)
                .and()
                .contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(41)
    public void testCustomUnitAppendToGaugeName() {
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);
        given().header(wantPromMetricsFormat).get("/metrics?scope=application").then().statusCode(200)
                .and().body(containsString(
                        "TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_gaugeMeB_hands gauge"));
    }

    @Test
    @RunAsClient
    @InSequence(42)
    public void testCustomUnitForCounter() {
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);
        given().header(wantPromMetricsFormat).get("/metrics?scope=application").then().statusCode(200)
                .and()
                .body(anyOf(containsString("TYPE metricTest_test1_countMeB_jellybean_total counter"),
                        containsString("TYPE metricTest_test1_countMeB_jellybean counter")));
    }

    /**
     * Check that there is at least one metric named gc.total and that they all contain expected tags (actually this is
     * just 'name' for now).
     */
    @Test
    @RunAsClient
    @InSequence(43)
    public void testGcCountMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);
        String data = given().header(wantPromMetricsFormat).get("/metrics?scope=base").asString();

        Map<String, MiniMeta> baseNames = getExpectedMetadataFromXmlFile(MetricRegistry.BASE_SCOPE);
        MiniMeta gcCountMetricMeta = baseNames.get("gc.total");
        Set<String> expectedTags = gcCountMetricMeta.tags.keySet();

        String[] lines = data.split("\n");

        boolean found = false;
        for (String line : lines) {
            // explicitly check for the metric line wth value (i.e. the use of `{`)
            if (line.contains("gc_total{")) {
                for (String expectedTag : expectedTags) {
                    assertThat("The metric should contain a " + expectedTag + " tag", line,
                            containsString(expectedTag + "="));
                }
                /*
                 * example: gc_total{name="global",mp_scope="base",tier="integration",} 14.0 Expect only one space
                 */
                String[] tmp = line.split(" ");
                assertEquals("Unexpected format: " + line, tmp.length, 2);

                Assert.assertTrue("gc.total value should be numeric and not negative",
                        Double.valueOf(tmp[1]).doubleValue() >= 0);

                found = true;
            }
        }

        Assert.assertTrue("At least one metric named gc.total is expected", found);
    }

    /**
     * Check that there is at least one metric named gc.time and that they all contain expected tags (actually this is
     * just 'name' for now).
     */
    @Test
    @RunAsClient
    @InSequence(44)
    public void testGcTimeMetrics() {
        Assume.assumeFalse(Boolean.getBoolean("skip.base.metric.tests"));
        Header wantPromMetricsFormat = new Header("Accept", TEXT_PLAIN);
        String data = given().header(wantPromMetricsFormat).get("/metrics?scope=base").asString();

        Map<String, MiniMeta> baseNames = getExpectedMetadataFromXmlFile(MetricRegistry.BASE_SCOPE);
        MiniMeta gcCountMetricMeta = baseNames.get("gc.time");
        Set<String> expectedTags = gcCountMetricMeta.tags.keySet();

        String[] lines = data.split("\n");

        boolean found = false;
        for (String line : lines) {
            // explicitly check for the metric line wth value (i.e. the use of `{`)
            if (line.contains("gc_time_seconds_total{")) {
                for (String expectedTag : expectedTags) {
                    assertThat("The metric should contain a " + expectedTag + " tag", line,
                            containsString(expectedTag + "="));
                }
                /*
                 * example: gc_time_total{name="scavenge",mp_scope="base",tier="integration",} 264.0 Expect only one
                 * space
                 */
                String[] tmp = line.split(" ");
                assertEquals("Unexpected format: " + line, tmp.length, 2);

                Assert.assertTrue("gc.total value should be numeric and not negative",
                        Double.valueOf(tmp[1]).doubleValue() >= 0);

                found = true;
            }
        }

        Assert.assertTrue("At least one metric named gc.total is expected", found);
    }

    /**
     * Test that multi-dimensional metrics are represented properly in Prometheus. WILL TEST FOR TYPE, HELP, VALUE LINES
     * This in effect tests for "metadata" as well
     */
    @Test
    @RunAsClient
    @InSequence(45)
    public void testMultipleTaggedMetricsProm() {
        Header wantPrometheus = new Header("Accept", TEXT_PLAIN);

        Response resp = given().header(wantPrometheus).get("/metrics?scope=application");

        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.clone(resp);
        responseBuilder.setBody(filterOutAppLabelPromMetrics(resp.getBody().asString()));
        resp = responseBuilder.build();

        /*
         * This test's primary objective is to ensure that the format is correct.
         */
        resp.then().statusCode(200)
                /**
                 * COUNTERS
                 */
                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_noTagCounter_total"))
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_noTagCounter_total counter"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_noTagCounter_total{mp_scope=\"application\",tier=\"integration\"} 0"))

                .body(containsString("# HELP org_eclipse_microprofile_metrics_test_MetricAppBean_taggedCounter_total"))
                .body(containsString(
                        "# TYPE org_eclipse_microprofile_metrics_test_MetricAppBean_taggedCounter_total counter"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_taggedCounter_total{mp_scope=\"application\",number=\"one\",tier=\"integration\"} 0"))
                .body(containsString(
                        "org_eclipse_microprofile_metrics_test_MetricAppBean_taggedCounter_total{mp_scope=\"application\",number=\"two\",tier=\"integration\"} 0"))
                /**
                 * GAUGES
                 */
                .body(containsString("# HELP taggedGauge"))
                .body(containsString("# TYPE taggedGauge gauge"))
                .body(containsString(
                        "taggedGauge{mp_scope=\"application\",number=\"one\",tier=\"integration\"} 1000"))
                .body(containsString(
                        "taggedGauge{mp_scope=\"application\",number=\"two\",tier=\"integration\"} 1000"))

                /*
                 * HISTOGRAMS ONLY CHECKING THAT COUNT IS 0
                 */

                // no tag
                .body(containsString("# HELP noTagHistogram_marshmallow"))
                .body(containsString("# TYPE noTagHistogram_marshmallow summary"))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "noTagHistogram_marshmallow_count{mp_scope=\"application\",tier=\"integration\"} 0"))
                .body(containsString(
                        "noTagHistogram_marshmallow_sum{mp_scope=\"application\",tier=\"integration\"} "))
                .body(containsString("# HELP noTagHistogram_marshmallow_max"))
                .body(containsString("# TYPE noTagHistogram_marshmallow_max gauge"))
                .body(containsString(
                        "noTagHistogram_marshmallow_max{mp_scope=\"application\",tier=\"integration\"} "))

                // tagged histo
                .body(containsString("# HELP taggedHistogram_marshmallow"))
                .body(containsString("# TYPE taggedHistogram_marshmallow summary"))
                // number=one
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow_count{mp_scope=\"application\",number=\"one\",tier=\"integration\"} 0"))
                .body(containsString(
                        "taggedHistogram_marshmallow_sum{mp_scope=\"application\",number=\"one\",tier=\"integration\"} "))
                .body(containsString("# HELP taggedHistogram_marshmallow_max"))
                .body(containsString("# TYPE taggedHistogram_marshmallow_max gauge"))
                .body(containsString(
                        "taggedHistogram_marshmallow_max{mp_scope=\"application\",number=\"one\",tier=\"integration\"} "))

                // number=two
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow_count{mp_scope=\"application\",number=\"two\",tier=\"integration\"} 0"))
                .body(containsString(
                        "taggedHistogram_marshmallow_sum{mp_scope=\"application\",number=\"two\",tier=\"integration\"} "))
                .body(containsString(
                        "taggedHistogram_marshmallow_max{mp_scope=\"application\",number=\"two\",tier=\"integration\"} "))

                /*
                 * TIMERS ONLY CHECKING THAT COUNT IS 0
                 */
                // no tag
                .body(containsString("# HELP noTagTimer_seconds"))
                .body(containsString("# TYPE noTagTimer_seconds summary"))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "noTagTimer_seconds{mp_scope=\"application\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "noTagTimer_seconds_count{mp_scope=\"application\",tier=\"integration\"} 0"))
                .body(containsString(
                        "noTagTimer_seconds_sum{mp_scope=\"application\",tier=\"integration\"} "))
                .body(containsString("# HELP noTagTimer_seconds_max"))
                .body(containsString("# TYPE noTagTimer_seconds_max gauge"))
                .body(containsString(
                        "noTagTimer_seconds_max{mp_scope=\"application\",tier=\"integration\"} "))

                // tagged timer
                .body(containsString("# HELP taggedTimer_seconds"))
                .body(containsString("# TYPE taggedTimer_seconds summary"))
                // number=one
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"one\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "taggedTimer_seconds_count{mp_scope=\"application\",number=\"one\",tier=\"integration\"} 0"))
                .body(containsString(
                        "taggedTimer_seconds_sum{mp_scope=\"application\",number=\"one\",tier=\"integration\"} "))
                .body(containsString("# HELP taggedTimer_seconds_max"))
                .body(containsString("# TYPE taggedTimer_seconds_max gauge"))
                .body(containsString(
                        "taggedTimer_seconds_max{mp_scope=\"application\",number=\"one\",tier=\"integration\"} "))

                // number=two
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.5\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.75\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.95\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.98\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.99\"} "))
                .body(containsString(
                        "taggedTimer_seconds{mp_scope=\"application\",number=\"two\",tier=\"integration\",quantile=\"0.999\"} "))
                .body(containsString(
                        "taggedTimer_seconds_count{mp_scope=\"application\",number=\"two\",tier=\"integration\"} 0"))
                .body(containsString(
                        "taggedTimer_seconds_sum{mp_scope=\"application\",number=\"two\",tier=\"integration\"} "))
                .body(containsString(
                        "taggedTimer_seconds_max{mp_scope=\"application\",number=\"two\",tier=\"integration\"} "));

    }

    private Map<String, MiniMeta> getExpectedMetadataFromXmlFile(String scope) {
        ClassLoader cl = this.getClass().getClassLoader();
        String fileName;
        switch (scope) {
            case "base" :
                fileName = "base_metrics.xml";
                break;
            case "application" :
                fileName = "application_metrics.xml";
                break;
            default :
                throw new IllegalArgumentException("No definitions for " + scope + " supported");
        }
        InputStream is = cl.getResourceAsStream(fileName);

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try {
            builder = fac.newDocumentBuilder();
            document = builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        Element root = (Element) document.getElementsByTagName("config").item(0);
        NodeList metrics = root.getElementsByTagName("metric");
        Map<String, MiniMeta> metaMap = new HashMap<>(metrics.getLength());
        for (int i = 0; i < metrics.getLength(); i++) {
            Element metric = (Element) metrics.item(i);
            MiniMeta mm = new MiniMeta();
            mm.multi = Boolean.parseBoolean(metric.getAttribute("multi"));
            mm.name = metric.getAttribute("name");
            mm.type = metric.getAttribute("type");
            mm.unit = metric.getAttribute("unit");
            mm.description = metric.getAttribute("description");

            mm.optional = Boolean.parseBoolean(metric.getAttribute("optional"));
            String tags = metric.getAttribute("tags");
            if (!(tags == null || tags.length() == 0)) {
                for (String tag : tags.split(",")) {
                    String[] str = tag.split("=");
                    mm.tags.put(str[0], str[1]);
                }
            }
            metaMap.put(mm.name, mm);
        }
        return metaMap;

    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static class MiniMeta {
        private String name;
        private String type;
        private String unit;
        private String description;
        private boolean multi;
        private boolean optional;
        private Map<String, String> tags = new TreeMap<>();

        public MiniMeta() {
            tags.put("tier", "integration");
        }

        String toPromString() {
            String out = name.replace('-', '_').replace('.', '_').replace(' ', '_');
            if (!unit.equals("none")) {
                out = out + "_" + unit;
            }
            out = out.replace("__", "_");
            out = out.replace(":_", ":");

            return out;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MiniMeta{");
            sb.append("name='").append(name).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", unit='").append(unit).append('\'');
            sb.append(", multi=").append(multi);
            sb.append(", optional=").append(optional);
            sb.append(", description=").append(description);
            sb.append('}');
            return sb.toString();
        }
    }
}
