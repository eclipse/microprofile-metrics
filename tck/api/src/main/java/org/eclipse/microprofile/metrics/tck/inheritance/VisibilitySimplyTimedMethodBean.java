/*
 * ********************************************************************
 *  Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.metrics.tck.inheritance;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

public class VisibilitySimplyTimedMethodBean {

    @SimplyTimed
    public void publicSimplyTimedMethod() {
    }

    @SimplyTimed
    void packagePrivateSimplyTimedMethod() {
    }

    @SimplyTimed
    protected void protectedSimplyTimedMethod() {
    }

    // @SimplyTimed
    // FIXME: It appears that OWB does not support interception of private
    // method while Weld does (before version 2.2.7.Final). Neither the CDI nor
    // Java Interceptors specifications make that point explicit though it may
    // be induced for a method invocation to be considered a business method
    // invocation that the method must be non-private and non-static.
    // http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#biz_method
    // http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#binding_interceptor_to_bean
    private void privateSimplyTimedMethod() {
    }
}
