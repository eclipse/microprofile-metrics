/*
 **********************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.test.optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
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
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(Arquillian.class)
public class MPMetricBaseMetricsTest {

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
        WebArchive jar = ShrinkWrap.create(WebArchive.class).addAsWebInfResource("META-INF/beans.xml", "beans.xml");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    @RunAsClient
    @InSequence(1)
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
    @InSequence(2)
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
    @InSequence(3)
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
    @InSequence(4)
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
                    assertEquals("Expected [" + mm + "] got [" + line + "]", tmp[3], mm.type);
                }
            }
            assertTrue("Not found [" + mm + "]", found);

        }
    }

    @Test
    @RunAsClient
    @InSequence(5)
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

    /**
     * Check that there is at least one metric named gc.total and that they all contain expected tags (actually this is
     * just 'name' for now).
     */
    @Test
    @RunAsClient
    @InSequence(6)
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
                final Pattern gcTotalPattern = Pattern.compile("(gc_total\\{.*?\\}) (\\d+\\.\\d+)");
                assertThat("Line format should be gc_total\\{.*?\\} \\d+\\.\\d+",
                        gcTotalPattern.matcher(line).matches());

                final String metricID = gcTotalPattern.matcher(line).replaceAll("$1");
                final String tags = metricID.replaceAll("^gc_total\\{", "").replaceAll("\\}$", "");

                for (String expectedTag : expectedTags) {
                    assertThat("The metric should contain a " + expectedTag + " tag", tags,
                            containsString(expectedTag + "="));
                }
                final String value = gcTotalPattern.matcher(line).replaceAll("$2");
                Assert.assertTrue("gc.total value should be numeric and not negative",
                        Double.valueOf(value).doubleValue() >= 0);

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
    @InSequence(7)
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
                final Pattern gcTimeTotalPattern = Pattern.compile("(gc_time_seconds_total\\{.*?\\}) (\\d+\\.\\d+)");
                assertThat("Line format should be gc_time_seconds_total\\{.*?\\} \\d+\\.\\d+",
                        gcTimeTotalPattern.matcher(line).matches());

                final String metricID = gcTimeTotalPattern.matcher(line).replaceAll("$1");
                final String tags = metricID.replaceAll("^gc_time_seconds_total\\{", "").replaceAll("\\}$", "");

                for (String expectedTag : expectedTags) {
                    assertThat("The metric should contain a " + expectedTag + " tag", tags,
                            containsString(expectedTag + "="));
                }
                final String value = gcTimeTotalPattern.matcher(line).replaceAll("$2");
                Assert.assertTrue("gc.time.seconds.total value should be numeric and not negative",
                        Double.valueOf(value).doubleValue() >= 0);

                found = true;
            }
        }

        Assert.assertTrue("At least one metric named gc.time.seconds.total is expected", found);
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


