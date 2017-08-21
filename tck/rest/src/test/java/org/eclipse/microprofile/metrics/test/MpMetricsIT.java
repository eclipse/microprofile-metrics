/*
    Licensed under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package org.eclipse.microprofile.metrics.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;

import com.jayway.restassured.response.Header;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Rest Test Kit
 * @author Heiko W. Rupp <hrupp@redhat.com>
 */
public class MpMetricsIT  {

  private static final String APPLICATION_JSON = "application/json";
  private static final String TEXT_PLAIN = "text/plain";

  private static final Header wantJson = new Header("Accept", APPLICATION_JSON);
  private static final Header wantPrometheusFormat = new Header("Accept",TEXT_PLAIN);

  private static final String DEFAULT_SERVER_URL = "http://localhost:8080";

  String[] baseNames = {"usedHeapMemory","committedHeapMemory","maxHeapMemory","jvmUptime","threadCount",
      "daemonThreadCount","peakThreadCount","currentLoadedClassCount","totalLoadedClassCount",
      "totalUnloadedClassCount","availableProcessors","systemLoadAverage"};


  @BeforeClass
  static public void setup() {
    String serverUrl = System.getProperty("test.url",DEFAULT_SERVER_URL);
    RestAssured.baseURI = serverUrl;
  }

  @Test
  public void testBadSubTreeWillReturn404() {
    when().get("/metrics/bad-tree")
        .then()
        .statusCode(404);
  }

  @Test
  public void testListsAllJson() {
    given()
        .header(wantJson)
        .when()
        .get("/metrics")
        .then()
        .statusCode(200)
        .and().contentType(APPLICATION_JSON);

    Map response =
        given()
            .header(wantJson)
            .when()
            .get("/metrics")
            .as(Map.class);

    assert response.containsKey("base");
    assert response.containsKey("vendor");
    assert response.containsKey("application");
  }

  @Test
  public void testListsAllPrometheus() {
    given()
        .header(wantPrometheusFormat)
        .when()
        .get("/metrics")
        .then()
        .statusCode(200)
        .and().contentType("text/plain");
  }


  @Test
  public void testBase() {
    given()
        .header("Accept", APPLICATION_JSON)
        .when().get("/metrics/base")
        .then()
        .statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("totalStartedThreadCount"));
  }

  @Test
  public void testBasePrometheus() {
    given()
        .header("Accept","text/plain")
        .when().get("/metrics/base")
        .then()
        .statusCode(200)
        .and().contentType("text/plain")
        .and()
        .body(containsString("# TYPE base:total_started_thread_count"),
              containsString("base:total_started_thread_count{tier=\"integration\"}"));
  }

  @Test
  public void testBaseAttributeJson() {
    given()
        .header(wantJson)
        .when().get("/metrics/base/totalStartedThreadCount")
        .then()
        .statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("totalStartedThreadCount"));
  }

  @Test
  public void testBaseSingluarItemsPresent() {

    JsonPath jsonPath =
    given()
        .header(wantJson)
        .get("/metrics/base")
        .jsonPath();

    Map<String, Object> elements = jsonPath.getMap(".");
    List<String> missing = new ArrayList<>();

    for (String item: baseNames) {
      if (!elements.containsKey(item)) {
        missing.add(item);
      }
    }

    assert missing.isEmpty() : "Following base items are missing: " + Arrays.toString(missing.toArray());
  }


  @Test
  public void testBaseAttributePrometheus() {
    given()
        .header("Accept","text/plain")
        .when().get("/metrics/base/totalStartedThreadCount")
        .then()
        .statusCode(200)
        .and().contentType("text/plain")
        .and()
        .body(containsString("# TYPE base:total_started_thread_count"),
              containsString("base:total_started_thread_count{tier=\"integration\"}"));
  }


  @Test
  public void testBaseMetadata() {
    given()
        .header(wantJson)
        .options("/metrics/base")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON);
  }

  @Test
  public void testBaseMetadataSingluarItems() {

    JsonPath jsonPath =
    given()
        .header(wantJson)
        .options("/metrics/base")
        .jsonPath();

    Map<String, Object> elements = jsonPath.getMap(".");
    List<String> missing = new ArrayList<>();

    for (String item: baseNames) {
      if (!elements.containsKey(item)) {
        missing.add(item);
      }
    }

    assert missing.isEmpty() : "Following base items are missing: " + Arrays.toString(missing.toArray());
  }

  @Test
  public void testBaseMetadataGarbageCollection() throws Exception {

    JsonPath jsonPath =
    given()
        .header(wantJson)
        .options("/metrics/base")
        .jsonPath();

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
  public void testApplicationMetadataOkJson() {
    given()
        .header(wantJson)
        .options("/metrics/application")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
    ;
  }

  @Test
  public void testApplicationMetadata() {
    Map<String,Map> body = RestAssured.options("/metrics/application")
        .as(Map.class);

    assert body.size()==2;
    assert body.keySet().contains("ola");
    assert body.keySet().contains("hello");

    for (Map.Entry<String, Map> mapEntry : body.entrySet()) {
      Map<String,Object> entry = mapEntry.getValue();
      String tags = (String) entry.get("tags");
      if (entry.get("name").equals("hello")) {

        assert entry.get("unit").equals("none");
        assert tags.contains("app=\"shop\"");
      }
      else if (entry.get("name").equals("ola")) {
        assert entry.get("unit").equals("none");
        assert tags.contains("app=\"ola\"");
      }
      else {
        throw new RuntimeException("Unexpected body element");
      }
    }
  }

  @Test
  public void testApplicationsData() {
    given()
        .header(wantJson)
        .when().get("/metrics/application")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("ola"),containsString("hello"));

  }

  @Test
  public void testApplicationsDataHello() {
    // Get the counter
    int count = when().get("/demo/count")
        .then().statusCode(200)
        .extract().path("hello");


    given()
        .header(wantJson)
        .when().get("/metrics/application/hello")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("\"hello\":"+count));
  }

  @Test
  public void testApplicationsDataHello2() {

    // Get the counter
    int count = when().get("/demo/count")
        .then().statusCode(200)
        .extract().path("hello");

    // Call hello world to bump the counter
    when().get("/demo/hello")
        .then().statusCode(200);
    count++;


    // Compare with what we got from the metrics api

    given()
        .header("Accept", MpMetricsIT.APPLICATION_JSON)
        .when().get("/metrics/application/hello")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("\"hello\":"+count));
  }

  @Test
  public void testUnitScalingPromMetricsName() {

    given()
        .header("Accept","text/plain")
        .when()
        .get("/metrics/vendor/mscLoadedModulesTime")
        .then()
        .statusCode(200)
        .and()
        .body(containsString("msc_loaded_modules_time_seconds"));
  }

  @Test
  public void testUnitScalingPromMetricsValue() {

    //  get value via unscaled json
    int val = when().get("/metrics/vendor/mscLoadedModulesTime")
        .then().statusCode(200)
        .extract().path("mscLoadedModulesTime");

    // Now get the one fro prom-api, which is scaled to seconds
    String response =
        given()
            .header("Accept","text/plain")
            .when()
            .get("/metrics/vendor/mscLoadedModulesTime")
            .asString();

    String[] lines = response.split("\n");

    // Find the line and see if this was correctly scaled
    // Entry data is in milliseconds and prom exports as seconds
    // so we need to divide by 1000
    for (String line : lines) {
      if (line.startsWith("vendor:msc_loaded")) {
        String[] items = line.split(" ");
        double theVal = Double.valueOf(items[1]);
        assert theVal == val / 1000d;
        return;
      }
    }
    throw new IllegalStateException("Should have found an entry");
  }


}
