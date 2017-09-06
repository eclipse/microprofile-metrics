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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Rest Test Kit
 * @author Heiko W. Rupp <hrupp@redhat.com>
 * @author Don Bourne <dbourne@ca.ibm.com>
 */
public class MpMetricsIT  {

  private static final String APPLICATION_JSON = "application/json";
  private static final String TEXT_PLAIN = "text/plain";

  private static final Header wantJson = new Header("Accept", APPLICATION_JSON);
  private static final Header wantPrometheusFormat = new Header("Accept",TEXT_PLAIN);

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT=8080;
  
  @BeforeClass
  static public void setup() throws MalformedURLException {
    String serverUrl = System.getProperty("test.url");

    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;

    if (serverUrl != null) {
        URL url = new URL(serverUrl);
        host = url.getHost();
        port = (url.getPort() == -1) ? DEFAULT_PORT : url.getPort();
    }

    RestAssured.baseURI = "http://" + host;
    RestAssured.port = port;
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
        .body(containsString("thread.max.count"));
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
        .body(containsString("# TYPE base:thread_max_count"),
              containsString("base:thread_max_count{tier=\"integration\"}"));
  }

  @Test
  public void testBaseAttributeJson() {
    given()
        .header(wantJson)
        .when().get("/metrics/base/thread.max.count")
        .then()
        .statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("thread.max.count"));
  }

  @Test
  public void testBaseSingularMetricsPresent() {

    JsonPath jsonPath =
    given()
        .header(wantJson)
        .get("/metrics/base")
        .jsonPath();

    Map<String, Object> elements = jsonPath.getMap(".");
    List<String> missing = new ArrayList<>();

    Map<String,MiniMeta> baseNames = getBaseMetrics();
    for (String item: baseNames.keySet()) {
      if (item.startsWith("gc.")) {
        continue;
      }
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
        .when().get("/metrics/base/thread.max.count")
        .then()
        .statusCode(200)
        .and().contentType("text/plain")
        .and()
        .body(containsString("# TYPE base:thread_max_count"),
              containsString("base:thread_max_count{tier=\"integration\"}"));
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

    Map<String,MiniMeta> baseNames = getBaseMetrics();
    for (String item: baseNames.keySet()) {
      if (item.startsWith("gc.")) {
        continue;
      }
      if (!elements.containsKey(item)) {
        missing.add(item);
      }
    }

    assert missing.isEmpty() : "Following base items are missing: " + Arrays.toString(missing.toArray());
  }

  @Test
  public void testBaseMetadataTypeAndUnit() {
    JsonPath jsonPath =
    given()
        .header(wantJson)
        .options("/metrics/base")
        .jsonPath();

    Map<String, Object> elements = jsonPath.getMap(".");

    Map<String,MiniMeta> expectedMetadata = getBaseMetrics();
    for (Map.Entry<String, MiniMeta> entry : expectedMetadata.entrySet()) {
      MiniMeta item = entry.getValue();
      if (item.name.startsWith("gc.")) {
        continue; // We don't deal with them here
      }
      Map<String,Object> fromServer = (Map<String, Object>) elements.get(item.name);
      assert item.type.equals(fromServer.get("type")) : "expected " + item.type + " but got " +
          fromServer.get("type") + " for " + item.name ;
      assert item.unit.equals(fromServer.get("unit")) : "expected " + item.unit + " but got " +
                fromServer.get("unit") + " for " + item.name ;
    }

  }

  @Test
  public void testPrometheusFormatNoBadChars() throws Exception {

    String data =
    given()
        .header(wantPrometheusFormat)
        .get("/metrics/base")
        .asString();

    String[] lines = data.split("\n");
    for (String line: lines) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] tmp = line.split(" ");
      assert tmp.length==2;
      assert !tmp[0].matches("[-.]") : "Line has illegal chars " + line;
      assert !tmp[0].matches("__") : "Found __ in " + line;
    }
  }

  /*
     * Technically Prometheus has no metadata call and this is
     * included inline in the response.
     */
  @Test
  public void testBaseMetadataSingluarItemsPrometheus() {

    String data =
    given()
        .header(wantPrometheusFormat)
        .get("/metrics/base")
        .asString();

    String[] lines = data.split("\n");

    Map<String,MiniMeta> expectedMetadata = getBaseMetrics();
    for (String line : lines) {
      if (!line.startsWith("# TYPE base:")) {
        continue;
      }
      String fullLine = line;
      int c = line.indexOf(":");
      line = line.substring(c+1);
      if (line.startsWith("gc_")) {
        continue;
      }
      boolean found = false;
      for (MiniMeta mm : expectedMetadata.values()) {
        String promName = mm.toPromString();
        String[] tmp = line.split(" ");
        assert tmp.length==2;
        if (tmp[0].startsWith(promName)) {
          found = true;
          assert tmp[1].equals(mm.type) : "Expected " + mm.toString() + " got " + fullLine;
        }
      }
      assert found : "Not found " + fullLine;

    }


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


  private Map<String,MiniMeta> getBaseMetrics()  {
    File f = new File("src/test/resources/base_metrics.xml");
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = fac.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  // TODO: Customise this generated block
    }
    Document document = null;
    try {
      document = builder.parse(f);
    } catch (SAXException|IOException e) {
      throw new RuntimeException(e);
    }

    Element root = (Element) document.getElementsByTagName("config").item(0);
    NodeList metrics = root.getElementsByTagName("metric");
    Map<String,MiniMeta> metaMap = new HashMap<>(metrics.getLength());
    for (int i = 0 ; i <  metrics.getLength(); i++) {
      Element metric = (Element) metrics.item(i);
      MiniMeta mm = new MiniMeta();
      mm.multi= Boolean.parseBoolean(metric.getAttribute("multi"));
      mm.name=metric.getAttribute("name");
      mm.type=metric.getAttribute("type");
      mm.unit=metric.getAttribute("unit");
      metaMap.put(mm.name,mm);
    }
    return metaMap;

  }

  private static class MiniMeta {
    String name;
    String type;
    String unit;
    boolean multi;

    String toPromString() {
      String out = name.replace('-', '_').replace('.', '_').replace(' ','_');
      out = out.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
      if (!unit.equals("none")) {
        out = out + "_" + getBaseUnitAsPrometheusString(unit);
      }
      out = out.replace("__","_");
      out = out.replace(":_",":");

      return out;
    }

    private String getBaseUnitAsPrometheusString(String unit) {
      String out;
      switch(unit) {
        case "ms" : out = "seconds"; break;
        case "byte" : out = "bytes"; break;

        default: out = "none";
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
      sb.append('}');
      return sb.toString();
    }
  }
}
