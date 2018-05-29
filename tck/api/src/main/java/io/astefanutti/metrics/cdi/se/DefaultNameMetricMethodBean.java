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

import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.ParallelCounted;
import org.eclipse.microprofile.metrics.annotation.Timed;

public class DefaultNameMetricMethodBean {

    @ParallelCounted
    public void defaultNameCountedMethod() {
    }

    @ParallelCounted(absolute = true)
    public void absoluteDefaultNameCountedMethod() {
    }

    @Metered
    public void defaultNameMeteredMethod() {
    }

    @Metered(absolute = true)
    public void absoluteDefaultNameMeteredMethod() {
    }

    @Timed
    public void defaultNameTimedMethod() {
    }

    @Timed(absolute = true)
    public void absoluteDefaultNameTimedMethod() {
    }
}
