/*
 **********************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasKey;

import com.jayway.restassured.response.Header;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Rest Test Kit
 *
 * @author Heiko W. Rupp <hrupp@redhat.com>
 * @author Don Bourne <dbourne@ca.ibm.com>
 */
@RunWith(Arquillian.class)
public class MpMetricTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";

    private static final String DEFAULT_PROTOCOL = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

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
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClass(MetricAppBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void testApplicationJsonResponseContentType() {
        Header acceptHeader = new Header("Accept", APPLICATION_JSON);

        given().header(acceptHeader).when().get("/metrics").then().statusCode(200).and().contentType(APPLICATION_JSON);

    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void testTextPlainResponseContentType() {
        Header acceptHeader = new Header("Accept", TEXT_PLAIN);

        given().header(acceptHeader).when().get("/metrics").then().statusCode(200).and().contentType(TEXT_PLAIN);
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void testBadSubTreeWillReturn404() {
        when().get("/metrics/bad-tree").then().statusCode(404);
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void testListsAllJson() {
        Header acceptHeader = new Header("Accept", APPLICATION_JSON);

        Map response = given().header(acceptHeader).when().get("/metrics").as(Map.class);

        // all servers should have some base metrics
        assert response.containsKey("base");

        // There may be application metrics, so check if the key exists and bail if it has no data
        if (response.containsKey("application")) {
          Map applicationData = (Map) response.get("application");
          assert applicationData.size()>0;
        }

        // There may be vendor metrics, so check if the key exists and bail if it has no data
        if (response.containsKey("vendor")) {
          Map vendorData = (Map) response.get("vendor");
          assert vendorData.size()>0;
        }
    }

    @Test
    @RunAsClient
    @InSequence(5)
    public void testBase() {
        given().header("Accept", APPLICATION_JSON).when().get("/metrics/base").then().statusCode(200).and()
                .contentType(MpMetricTest.APPLICATION_JSON).and().body(containsString("thread.max.count"));
    }

    @Test
    @RunAsClient
    @InSequence(6)
    public void testBasePrometheus() {
        given().header("Accept", TEXT_PLAIN).when().get("/metrics/base").then().statusCode(200).and()
                .contentType(TEXT_PLAIN).and().body(containsString("# TYPE base:thread_max_count"),
                        containsString("base:thread_max_count{tier=\"integration\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(7)
    public void testBaseAttributeJson() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        given().header(wantJson).when().get("/metrics/base/thread.max.count").then().statusCode(200).and()
                .contentType(MpMetricTest.APPLICATION_JSON).and().body(containsString("thread.max.count"));
    }

    @Test
    @RunAsClient
    @InSequence(8)
    public void testBaseSingularMetricsPresent() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).get("/metrics/base").jsonPath();

        Map<String, Object> elements = jsonPath.getMap(".");
        List<String> missing = new ArrayList<>();

        Map<String, MiniMeta> baseNames = getExpectedMetadataFromXmlFile(MetricRegistry.Type.BASE);
        for (String item : baseNames.keySet()) {
            if (item.startsWith("gc.")) {
                continue;
            }
            if (!elements.containsKey(item) && !baseNames.get(item).optional) {
                missing.add(item);
            }
        }

        assert missing.isEmpty() : "Following base items are missing: " + Arrays.toString(missing.toArray());
    }

    @Test
    @RunAsClient
    @InSequence(9)
    public void testBaseAttributePrometheus() {
        given().header("Accept", TEXT_PLAIN).when().get("/metrics/base/thread.max.count").then().statusCode(200).and()
                .contentType(TEXT_PLAIN).and().body(containsString("# TYPE base:thread_max_count"),
                        containsString("base:thread_max_count{tier=\"integration\"}"));
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testBaseMetadata() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        given().header(wantJson).options("/metrics/base").then().statusCode(200).and()
                .contentType(MpMetricTest.APPLICATION_JSON);

    }

    @Test
    @RunAsClient
    @InSequence(11)
    public void testBaseMetadataSingluarItems() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/base").jsonPath();

        Map<String, Object> elements = jsonPath.getMap(".");
        List<String> missing = new ArrayList<>();

        Map<String, MiniMeta> baseNames = getExpectedMetadataFromXmlFile(MetricRegistry.Type.BASE);
        for (String item : baseNames.keySet()) {
            if (item.startsWith("gc.") || baseNames.get(item).optional) {
                continue;
            }
            if (!elements.containsKey(item)) {
                missing.add(item);
            }
        }

        assert missing.isEmpty() : "Following base items are missing: " + Arrays.toString(missing.toArray());
    }

    @Test
    @RunAsClient
    @InSequence(12)
    public void testBaseMetadataTypeAndUnit() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/base").jsonPath();

        Map<String, Map<String, Object>> elements = jsonPath.getMap(".");

        Map<String, MiniMeta> expectedMetadata = getExpectedMetadataFromXmlFile(MetricRegistry.Type.BASE);
              checkMetadataPresent(elements, expectedMetadata);

    }

    private void checkMetadataPresent(Map<String, Map<String, Object>> elements, Map<String, MiniMeta> expectedMetadata) {
        for (Map.Entry<String, MiniMeta> entry : expectedMetadata.entrySet()) {
            MiniMeta item = entry.getValue();
            if (item.name.startsWith("gc.") || expectedMetadata.get(item.name).optional) {
                continue; // We don't deal with them here
            }
            Map<String, Object> fromServer = elements.get(item.name);
            assert item.type.equals(fromServer.get("type")) : "expected " + item.type + " but got "
                + fromServer.get("type") + " for " + item.name;
            assert item.unit.equals(fromServer.get("unit")) : "expected " + item.unit + " but got "
                + fromServer.get("unit") + " for " + item.name;
        }
    }

    @Test
    @RunAsClient
    @InSequence(13)
    public void testPrometheusFormatNoBadChars() throws Exception {
        Header wantPrometheusFormat = new Header("Accept", TEXT_PLAIN);

        String data = given().header(wantPrometheusFormat).get("/metrics/base").asString();

        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] tmp = line.split(" ");
            assert tmp.length == 2;
            assert !tmp[0].matches("[-.]") : "Line has illegal chars " + line;
            assert !tmp[0].matches("__") : "Found __ in " + line;
        }
    }

    /*
     * Technically Prometheus has no metadata call and this is included inline
     * in the response.
     */
    @Test
    @RunAsClient
    @InSequence(14)
    public void testBaseMetadataSingluarItemsPrometheus() {
        Header wantPrometheusFormat = new Header("Accept", TEXT_PLAIN);

        String data = given().header(wantPrometheusFormat).get("/metrics/base").asString();

        String[] lines = data.split("\n");

        Map<String, MiniMeta> expectedMetadata = getExpectedMetadataFromXmlFile(MetricRegistry.Type.BASE);
        for (MiniMeta mm : expectedMetadata.values()) {

            boolean found = false;
            // Skip GC
            if (mm.name.startsWith("gc.") || expectedMetadata.get(mm.name).optional) {
                continue;
            }
            for (String line : lines) {
                if (!line.startsWith("# TYPE base:")) {
                    continue;
                }
                String fullLine = line;
                int c = line.indexOf(":");
                line = line.substring(c + 1);
                String promName = mm.toPromString();
                String[] tmp = line.split(" ");
                assert tmp.length == 2;
                if (tmp[0].startsWith(promName)) {
                    found = true;
                    assert tmp[1].equals(mm.type) : "Expected [" + mm.toString() + "] got [" + fullLine + "]";
                }
            }
            assert found : "Not found [" + mm.toString() + "]";

        }
    }

    @Test
    @RunAsClient
    @InSequence(15)
    public void testBaseMetadataGarbageCollection() throws Exception {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/base").jsonPath();

        int count = 0;
        Map<String, Object> elements = jsonPath.getMap(".");
        for (String name : elements.keySet()) {
            if (name.startsWith("gc.")) {
                assert name.endsWith(".count") || name.endsWith(".time");
                count++;
            }
        }
        assert count > 0;
    }

    @Test
    @RunAsClient
    @InSequence(16)
    public void testApplicationMetadataOkJson() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        Response response = given().header(wantJson).options("/metrics/application");
        int code = response.getStatusCode();

        assert code == 200 || code == 204;

    }

    @Test
    @InSequence(17)
    public void testSetupApplicationMetrics() {

        metricAppBean.countMe();
        metricAppBean.countMeA();

        metricAppBean.gaugeMe();
        metricAppBean.gaugeMeA();

        metricAppBean.histogramMe();

        metricAppBean.meterMe();
        metricAppBean.meterMeA();

        metricAppBean.timeMe();
        metricAppBean.timeMeA();

    }

    @Test
    @RunAsClient
    @InSequence(18)
    public void testApplicationMetricsJSON() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        given().header(wantJson).get("/metrics/application").then().statusCode(200)

                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.redCount'", equalTo(0))

                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.blue'", equalTo(0))

                .body("greenCount", equalTo(0))

                .body("purple", equalTo(0))

                .body("'metricTest.test1.count'", equalTo(1))

                .body("'metricTest.test1.countMeA'", equalTo(1))

                .body("'metricTest.test1.gauge'", equalTo(19))

                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.gaugeMeA'", equalTo(1000))

                .body("'metricTest.test1.histogram'.count", equalTo(1000))
                .body("'metricTest.test1.histogram'.max", equalTo(999))
                .body("'metricTest.test1.histogram'.mean", equalTo((float) 499.5))
                .body("'metricTest.test1.histogram'.min", equalTo(0))
                .body("'metricTest.test1.histogram'.p50", equalTo((float) 499.0))
                .body("'metricTest.test1.histogram'.p75", equalTo((float) 749))
                .body("'metricTest.test1.histogram'.p95", equalTo((float) 949))
                .body("'metricTest.test1.histogram'.p98", equalTo((float) 979))
                .body("'metricTest.test1.histogram'.p99", equalTo((float) 989))
                .body("'metricTest.test1.histogram'.p999", equalTo((float) 998))
                .body("'metricTest.test1.histogram'", hasKey("stddev"))

                .body("'metricTest.test1.meter'.count", equalTo(1))
                .body("'metricTest.test1.meter'", hasKey("fifteenMinRate"))
                .body("'metricTest.test1.meter'", hasKey("fiveMinRate"))
                .body("'metricTest.test1.meter'", hasKey("meanRate"))
                .body("'metricTest.test1.meter'", hasKey("oneMinRate"))

                .body("meterMeA.count", equalTo(1)).body("meterMeA", hasKey("fifteenMinRate"))
                .body("meterMeA", hasKey("fiveMinRate")).body("meterMeA", hasKey("meanRate"))
                .body("meterMeA", hasKey("oneMinRate"))

                .body("'metricTest.test1.timer'.count", equalTo(1))
                .body("'metricTest.test1.timer'", hasKey("fifteenMinRate"))
                .body("'metricTest.test1.timer'", hasKey("fiveMinRate"))
                .body("'metricTest.test1.timer'", hasKey("meanRate"))
                .body("'metricTest.test1.timer'", hasKey("oneMinRate")).body("'metricTest.test1.timer'", hasKey("max"))
                .body("'metricTest.test1.timer'", hasKey("mean")).body("'metricTest.test1.timer'", hasKey("min"))
                .body("'metricTest.test1.timer'", hasKey("p50")).body("'metricTest.test1.timer'", hasKey("p75"))
                .body("'metricTest.test1.timer'", hasKey("p95")).body("'metricTest.test1.timer'", hasKey("p98"))
                .body("'metricTest.test1.timer'", hasKey("p99")).body("'metricTest.test1.timer'", hasKey("p999"))
                .body("'metricTest.test1.timer'", hasKey("stddev"))

                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'.count", equalTo(1))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("fifteenMinRate"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("fiveMinRate"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("meanRate"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("oneMinRate"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("max"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("mean"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("min"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p50"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p75"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p95"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p98"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p99"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("p999"))
                .body("'org.eclipse.microprofile.metrics.test.MetricAppBean.timeMeA'", hasKey("stddev"));
    }

    @Test
    @RunAsClient
    @InSequence(19)
    public void testApplicationMetadataItems() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/application").jsonPath();

        Map<String, Object> elements = jsonPath.getMap(".");
        List<String> missing = new ArrayList<>();

        Map<String, MiniMeta> names = getExpectedMetadataFromXmlFile(MetricRegistry.Type.APPLICATION);
        for (String item : names.keySet()) {
            if (!elements.containsKey(item)) {
                missing.add(item);
            }
        }

        assert missing.isEmpty() : "Following application items are missing: " + Arrays.toString(missing.toArray());
    }

    @Test
    @RunAsClient
    @InSequence(20)
    public void testApplicationMetadataTypeAndUnit() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/application").jsonPath();

        Map<String, Map<String, Object>> elements = jsonPath.getMap(".");

        Map<String, MiniMeta> expectedMetadata = getExpectedMetadataFromXmlFile(MetricRegistry.Type.APPLICATION);
        checkMetadataPresent(elements, expectedMetadata);

        assert missing.isEmpty() : "Following application items are missing: " + Arrays.toString(missing.toArray());
    }
    
    @Test
    @RunAsClient
    @InSequence(21)
    public void testApplicationTagJson() {

        JsonPath jsonPath =  given().header("Accept", APPLICATION_JSON)
            .when()
            .options("/metrics/application/purple").jsonPath();
        String tags = jsonPath.getString("purple.tags");
        assert tags != null;
        assert tags.contains("app=myShop");
        assert tags.contains("tier=integration");
    }

    @Test
    @RunAsClient
    @InSequence(22)
    public void testApplicationTagPrometheus() {

        given().header("Accept", TEXT_PLAIN).when().get("/metrics/application/purple")
            .then().statusCode(200)
            .and()
            .body(containsString("tier=\"integration\""))
            .body(containsString("app=\"myShop\""));
    }

    @Test
    @RunAsClient
    @InSequence(28)
    public void testConvertingToBaseUnit() {
        Header wantPrometheusFormat = new Header("Accept", TEXT_PLAIN);
        given().header(wantPrometheusFormat).get("/metrics/application").then().statusCode(200)
        .and().body(containsString("TYPE application:org_eclipse_microprofile_metrics_test_metric_app_bean_gauge_me_a_bytes gauge"))
        .and().body(containsString("TYPE application:metric_test_test1_gauge_bytes gauge"));

        
    }
    
    @Test
    @RunAsClient
    @InSequence(29)
    public void testNonStandardUnitsJSON() {
        
        Header wantJSONFormat = new Header("Accept", APPLICATION_JSON);
        given().header(wantJSONFormat).options("/metrics/application/jellybeanHistogram").then().statusCode(200)
        .body("jellybeanHistogram.unit", equalTo("jellybeans"));
        
    }
    
    @Test
    @RunAsClient
    @InSequence(30)
    public void testNonStandardUnitsPrometheus() {
        
        String prefix = "jellybean_histogram_";

        Header wantPrometheusFormat = new Header("Accept", TEXT_PLAIN);
        given().header(wantPrometheusFormat).get("/metrics/application/jellybeanHistogram").then().statusCode(200)
        .and()
        .body(containsString(prefix + "jellybeans_count"))
        .body(containsString("# TYPE application:" + prefix + "jellybeans summary"))
        .body(containsString(prefix + "mean_jellybeans"))
        .body(containsString(prefix + "min_jellybeans"))
        .body(containsString(prefix + "max_jellybeans"))
        .body(containsString(prefix + "stddev_jellybeans"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.5\"}"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.75\"}"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.95\"}"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.98\"}"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.99\"}"))
        .body(containsString(prefix + "jellybeans{tier=\"integration\",quantile=\"0.999\"}"));
    }
    
    @Test
    @RunAsClient
    @InSequence(31)
    public void testOptionalBaseMetrics() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/base").jsonPath();

        Map<String, Object> elements = jsonPath.getMap(".");
        Map<String, MiniMeta> names = getExpectedMetadataFromXmlFile(MetricRegistry.Type.BASE);
        
        for (String item : names.keySet()) {
            if (elements.containsKey(item) && names.get(item).optional) {
                String prefix = names.get(item).name;
                String type = "\""+prefix+"\""+".type";
                String unit= "\""+prefix+"\""+".unit";
                
                given().header(wantJson).options("/metrics/base/"+prefix).then().statusCode(200)
                .body(type, equalTo(names.get(item).type))
                .body(unit, equalTo(names.get(item).unit));
            }
        }
        
    }

    private Map<String, MiniMeta> getExpectedMetadataFromXmlFile(MetricRegistry.Type scope) {
      ClassLoader cl = this.getClass().getClassLoader();
      String fileName;
      switch (scope) {
          case BASE:
              fileName = "base_metrics.xml";
              break;
          case APPLICATION:
              fileName = "application_metrics.xml";
              break;
          default:
              throw new IllegalArgumentException("No definitions for " + scope.getName() + " supported");
      }
      InputStream is = cl.getResourceAsStream(fileName);

      DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder;
      Document document;
      try {
          builder = fac.newDocumentBuilder();
          document = builder.parse(is);
      }
      catch (ParserConfigurationException | SAXException | IOException e) {
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
          mm.optional = Boolean.parseBoolean(metric.getAttribute("optional"));
          metaMap.put(mm.name, mm);
      }
      return metaMap;

  }

    @Test
    @RunAsClient
    @InSequence(20)
    public void testApplicationMetadataTypeAndUnit() {
        Header wantJson = new Header("Accept", APPLICATION_JSON);

        JsonPath jsonPath = given().header(wantJson).options("/metrics/application").jsonPath();

        Map<String, Map<String, Object>> elements = jsonPath.getMap(".");

        Map<String, MiniMeta> expectedMetadata = getExpectedMetadataFromXmlFile(MetricRegistry.Type.APPLICATION);
        checkMetadataPresent(elements, expectedMetadata);

    }

    @Test
    @RunAsClient
    @InSequence(21)
    public void testApplicationTagJson() {

        JsonPath jsonPath =  given().header("Accept", APPLICATION_JSON)
            .when()
            .options("/metrics/application/purple").jsonPath();
        String tags = jsonPath.getString("purple.tags");
        assert tags != null;
        assert tags.contains("app=myShop");
        assert tags.contains("tier=integration");
    }

    @Test
    @RunAsClient
    @InSequence(22)
    public void testApplicationTagPrometheus() {

        given().header("Accept", TEXT_PLAIN).when().get("/metrics/application/purple")
            .then().statusCode(200)
            .and()
            .body(containsString("tier=\"integration\""))
            .body(containsString("app=\"myShop\""));
    }

    private Map<String, MiniMeta> getExpectedMetadataFromXmlFile(MetricRegistry.Type scope) {
      ClassLoader cl = this.getClass().getClassLoader();
      String fileName;
      switch (scope) {
          case BASE:
              fileName = "base_metrics.xml";
              break;
          case APPLICATION:
              fileName = "application_metrics.xml";
              break;
          default:
              throw new IllegalArgumentException("No definitions for " + scope.getName() + " supported");
      }
      InputStream is = cl.getResourceAsStream(fileName);

      DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder;
      Document document;
      try {
          builder = fac.newDocumentBuilder();
          document = builder.parse(is);
      }
      catch (ParserConfigurationException | SAXException | IOException e) {
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
          metaMap.put(mm.name, mm);
      }
      return metaMap;

  }

    private static class MiniMeta {
        private String name;
        private String type;
        private String unit;
        private boolean multi;
        private boolean optional;

        String toPromString() {
            String out = name.replace('-', '_').replace('.', '_').replace(' ', '_');
            out = out.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
            if (!unit.equals("none")) {
                out = out + "_" + getBaseUnitAsPrometheusString(unit);
            }
            out = out.replace("__", "_");
            out = out.replace(":_", ":");

            return out;
        }

        private String getBaseUnitAsPrometheusString(String unit) {
            String out;
            switch (unit) {
            case "milliseconds":
                out = "seconds";
                break;
            case "bytes":
                out = "bytes";
                break;
            case "percent":
                out = "percent";
                break;

            default:
                out = "none";
            }

            return out;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MiniMeta{");
            sb.append("name='").append(name).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", unit='").append(unit).append('\'');
            sb.append(", multi=").append(multi);
            sb.append(", optional =").append(optional);
            sb.append('}');
            return sb.toString();
        }
    }
}
