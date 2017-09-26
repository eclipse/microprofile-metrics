/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi.se;

import org.eclipse.microprofile.metrics.annotation.Timed;

public class VisibilityTimedMethodBean {

    @Timed
    public void publicTimedMethod() {
    }

    @Timed
    void packagePrivateTimedMethod() {
    }

    @Timed
    protected void protectedTimedMethod() {
    }

    // @Timed
    // FIXME: It appears that OWB does not support interception of private
    // method while Weld does (before version 2.2.7.Final). Neither the CDI nor
    // Java Interceptors specifications make that point explicit though it may
    // be induced for a method invocation to be considered a business method
    // invocation that the method must be non-private and non-static.
    // http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#biz_method
    // http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#binding_interceptor_to_bean
    private void privateTimedMethod() {
    }
}
