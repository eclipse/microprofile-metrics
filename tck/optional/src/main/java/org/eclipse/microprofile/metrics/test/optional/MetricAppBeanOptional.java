/*
 **********************************************************************
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.eclipse.microprofile.metrics.MetricRegistry;

@Path("/")
@ApplicationScoped
public class MetricAppBeanOptional {
    
    @Inject
    private MetricRegistry metrics;
    
    @GET
    @Path("/get-noparam")
    public String  getNoParam() throws Exception {
        return "This is a GET request with no parameters";
    }
      
    @OPTIONS
    @Path("/options-noparam")
    public String  optionsNoParam() throws Exception {
        return "This is a OPTIONS request with no parameters";
    }
 
    @HEAD
    @Path("/head-noparam")
    public String  headNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }
    
    @PUT
    @Path("/put-noparam")
    public String  putNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }
    
    @POST
    @Path("/post-noparam")
    public String  postNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }
    
    @DELETE
    @Path("/delete-noparam")
    public String  deleteNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }
    
    @GET
    @Path("/get-single-string-param")
    public String  getSingleStringParam(@QueryParam("qp1") String v1) throws Exception {
        return "This is a GET request with single string parameter";
    }
    
    @GET
    @Path("/get-single-int-param")
    public String  getSingleIntParam(@QueryParam("qp1") int v1) throws Exception {
        return "This is a GET request with single string parameter";
    }
    
    @GET
    @Path("/get-single-double-param")
    public String  getSingleDoubleParam(@QueryParam("qp1") double v1) throws Exception {
        return "This is a GET request with single int parameter";
    }
    
    @GET
    @Path("/get-single-long-param")
    public String  getSingleLongParam(@QueryParam("qp1") long v1) throws Exception {
        return "This is a GET request with single long parameter";
    }

    @GET
    @Path("/get-single-boolean-param")
    public String  getSingleBooleanParam(@QueryParam("qp1") boolean v1) throws Exception {
        return "This is a GET request with single boolean parameter";
    }
    
    @GET
    @Path("/get-context-params")
    public String  getContextParams(
            final @Context HttpHeaders httpheaders,
            final @Context Request request,
            final @Context UriInfo uriInfo,
            final @Context ResourceContext resourceContext,
            final @Context Providers providers,
            final @Context Application application,
            final @Context SecurityContext securityContext,
            final @Context Configuration configuration) throws Exception {
        
        return "This is a GET request with context parameters";
    }
    
    @GET
    @Path("/get-list-param1")
    public String  getListParam1(@QueryParam("qp1") List<String> v1) throws Exception {
        return "This is a GET request with a List1";
    }
    
    @GET
    @Path("/get-list-param2")
    public String  getListParam2(@QueryParam("qp1") List<Integer> v1) throws Exception {
        return "This is a GET request with a List2";
    }
    
    @GET
    @Path("/get-list-param3")
    public String  getListParam3(@QueryParam("qp1") List<Double> v1, @QueryParam("qp2") List<Long> v2) throws Exception {
        return "This is a GET request with a List3";
    }
    
    @GET
    @Path("/get-vararg-param1")
    public String  getVarargParam1(@QueryParam("qp1") Boolean... v1) throws Exception {
        return "This is a GET request with vararg parameter1";
    }
    
    @GET
    @Path("/get-vararg-param2")
    public String  getVarargParam2(@QueryParam("qp1") int v1, @QueryParam("qp2") String... v2) throws Exception {
        return "This is a GET request with vararg parameter2";
    }
    
    @GET
    @Path("/get-array-param1")
    public String  getArrayParam1(@QueryParam("qp1") String[] v1) throws Exception {
        return "This is a GET request with array parameter";
    }
    
    @GET
    @Path("/get-array-param2")
    public String  getArrayParam2(@QueryParam("qp1") int[] v1) throws Exception {
        return "This is a GET request with array parameter";
    }
    
    @GET
    @Path("/get-array-param3")
    public String  getArrayParam3(@QueryParam("qp1") Double[] v1) throws Exception {
        return "This is a GET request with array parameter";
    }

    
    @GET
    @Path("/get-multiple-param1")
    public String  getMultipleParam1(
            @QueryParam("qp1") boolean v1,
            @QueryParam("qp2") int v2,
            @QueryParam("qp3") double v3,
            @QueryParam("qp4") String v4,
            @QueryParam("qp5") long v5) throws Exception {
        return "This is a GET request with multiple parameters1";
    }
    
    @GET
    @Path("/get-multiple-param2")
    public String  getMultipleParam2(@QueryParam("qp1") String v1, @QueryParam("qp2") List<String> v2) throws Exception {
        return "This is a GET request with multiple parameters2";
    }
    
    @GET
    @Path("/get-multiple-param3")
    public String  getMultipleParam3(@QueryParam("qp1") boolean v1,
            @QueryParam("qp2") Boolean v2, @QueryParam("qp3") double v3, @QueryParam("qp4")String...v4) throws Exception {
        return "This is a GET request with multiple parameters3";
    }
    
    @GET
    @Path("/get-multiple-param4")
    public String  getMultipleParam4(@QueryParam("qp1") Set<String> v1,
            @QueryParam("qp2") SortedSet<Integer> v2, @QueryParam("qp3") double[] v3) throws Exception {
        return "This is a GET request with multiple parameters4";
    }
    
    
    @GET
    @Path("/get-name-object")
    public String  getNameObject(@QueryParam("qp1") NameObject v1) throws Exception {
        return "This is a GET request with NameObject";
    }
    
    @GET
    @Path("/get-async")
    public void  getAsync(@Suspended final AsyncResponse asyncResponse) throws Exception {
        Thread thread = new Thread ( () -> {
            try {
                Thread.sleep(5000);
                asyncResponse.resume("This is a GET request with AsyncResponse");
            }
            catch (Exception e) {
                System.err.println(e.toString());
            }
        });
        thread.start();
    }
    
    
    @POST
    @Path("/post-multiple-param1")
    public String  postMultipleParam1(
            @QueryParam("qp1") boolean v1,
            @QueryParam("qp2") int v2,
            @QueryParam("qp3") double v3,
            @QueryParam("qp4") String v4,
            @QueryParam("qp5") long v5) throws Exception {
        return "This is a POST request with multiple parameters1";
    }
    
    @POST
    @Path("/post-multiple-param2")
    public String  postMultipleParam2(@QueryParam("qp1") String v1, @QueryParam("qp2") List<String> v2) throws Exception {
        return "This is a POST request with multiple parameters2";
    }
    
    @POST
    @Path("/post-multiple-param3")
    public String  postMultipleParam3(@QueryParam("qp1") boolean v1,
            @QueryParam("qp2") Boolean v2, @QueryParam("qp3") double v3, @QueryParam("qp4")String...v4) throws Exception {
        return "This is a GPOSTET request with multiple parameters3";
    }
    
    @POST
    @Path("/post-multiple-param4")
    public String  postMultipleParam4(@QueryParam("qp1") Set<String> v1,
            @QueryParam("qp2") SortedSet<Integer> v2, @QueryParam("qp3") double[] v3) throws Exception {
        return "This is a POST request with multiple parameters4";
    }
    
    @POST
    @Path("/post-multiple-param5")
    public String  postMultipleParam5(@QueryParam("qp1") Set<String> v1,
            @QueryParam("qp2") Long v2, @QueryParam("qp3") Integer[] v3) throws Exception {
        return "This is a POST request with multiple parameters5";
    }
    
}
