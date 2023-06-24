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

import org.eclipse.microprofile.metrics.MetricRegistry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

@Path("/")
@ApplicationScoped
public class MetricAppBeanOptional {

    @Inject
    private MetricRegistry metrics;

    @GET
    @Path("/get-noparam")
    public String getNoParam() throws Exception {
        return "This is a GET request with no parameters";
    }

    @OPTIONS
    @Path("/options-noparam")
    public String optionsNoParam() throws Exception {
        return "This is a OPTIONS request with no parameters";
    }

    @HEAD
    @Path("/head-noparam")
    public String headNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }

    @PUT
    @Path("/put-noparam")
    public String putNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }

    @POST
    @Path("/post-noparam")
    public String postNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }

    @DELETE
    @Path("/delete-noparam")
    public String deleteNoParam() throws Exception {
        return "This is a HEAD request with no parameters";
    }

    @GET
    @Path("/get-single-string-param")
    public String getSingleStringParam(@QueryParam("qp1") String v1) throws Exception {
        return "This is a GET request with single string parameter";
    }

    @GET
    @Path("/get-single-int-param")
    public String getSingleIntParam(@QueryParam("qp1") int v1) throws Exception {
        return "This is a GET request with single string parameter";
    }

    @GET
    @Path("/get-single-double-param")
    public String getSingleDoubleParam(@QueryParam("qp1") double v1) throws Exception {
        return "This is a GET request with single int parameter";
    }

    @GET
    @Path("/get-single-long-param")
    public String getSingleLongParam(@QueryParam("qp1") long v1) throws Exception {
        return "This is a GET request with single long parameter";
    }

    @GET
    @Path("/get-single-boolean-param")
    public String getSingleBooleanParam(@QueryParam("qp1") boolean v1) throws Exception {
        return "This is a GET request with single boolean parameter";
    }

    @GET
    @Path("/get-context-params")
    public String getContextParams(
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
    public String getListParam1(@QueryParam("qp1") List<String> v1) throws Exception {
        return "This is a GET request with a List1";
    }

    @GET
    @Path("/get-list-param2")
    public String getListParam2(@QueryParam("qp1") List<Integer> v1) throws Exception {
        return "This is a GET request with a List2";
    }

    @GET
    @Path("/get-list-param3")
    public String getListParam3(@QueryParam("qp1") List<Double> v1, @QueryParam("qp2") List<Long> v2) throws Exception {
        return "This is a GET request with a List3";
    }

    @GET
    @Path("/get-multiple-param1")
    public String getMultipleParam1(
            @QueryParam("qp1") boolean v1,
            @QueryParam("qp2") int v2,
            @QueryParam("qp3") double v3,
            @QueryParam("qp4") String v4,
            @QueryParam("qp5") long v5) throws Exception {
        return "This is a GET request with multiple parameters1";
    }

    @GET
    @Path("/get-multiple-param2")
    public String getMultipleParam2(@QueryParam("qp1") String v1, @QueryParam("qp2") List<String> v2) throws Exception {
        return "This is a GET request with multiple parameters2";
    }

    @GET
    @Path("/get-multiple-param4")
    public String getMultipleParam4(@QueryParam("qp1") Set<String> v1,
            @QueryParam("qp2") SortedSet<Integer> v2) throws Exception {
        return "This is a GET request with multiple parameters4";
    }

    @GET
    @Path("/get-name-object")
    public String getNameObject(@QueryParam("qp1") NameObject v1) throws Exception {
        return "This is a GET request with NameObject";
    }

    @GET
    @Path("/get-async")
    public void getAsync(@Suspended final AsyncResponse asyncResponse) throws Exception {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                asyncResponse.resume("This is a GET request with AsyncResponse");
            } catch (Exception e) {
                System.err.println(e);
            }
        });
        thread.start();
    }

    @POST
    @Path("/post-multiple-param1")
    public String postMultipleParam1(
            @QueryParam("qp1") boolean v1,
            @QueryParam("qp2") int v2,
            @QueryParam("qp3") double v3,
            @QueryParam("qp4") String v4,
            @QueryParam("qp5") long v5) throws Exception {
        return "This is a POST request with multiple parameters1";
    }

    @POST
    @Path("/post-multiple-param2")
    public String postMultipleParam2(@QueryParam("qp1") String v1, @QueryParam("qp2") List<String> v2)
            throws Exception {
        return "This is a POST request with multiple parameters2";
    }

    @POST
    @Path("/post-multiple-param4")
    public String postMultipleParam4(@QueryParam("qp1") Set<String> v1,
            @QueryParam("qp2") SortedSet<Integer> v2) throws Exception {
        return "This is a POST request with multiple parameters4";
    }

    @GET
    @Path("/get-mapped-arithmetic-exception")
    public String getMappedArithException() throws Exception {
        return "This is a GET request to test mapped exceptions that throws an ArithmeticException";
    }

    @POST
    @Path("/post-mapped-arithmetic-exception")
    public String postMappedArithException() throws Exception {
        return "This is a POST request to test mapped exceptions that throws an ArithmeticException";
    }

    @GET
    @Path("/get-unmapped-exception")
    public String getUnmappedArithException() throws Exception {
        throw new IllegalArgumentException();
    }

    @POST
    @Path("/post-unmapped-exception")
    public String postUnmappedArithException() throws Exception {
        throw new IllegalArgumentException();
    }

}

