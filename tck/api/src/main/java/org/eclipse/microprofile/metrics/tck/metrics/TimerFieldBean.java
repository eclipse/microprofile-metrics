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
package org.eclipse.microprofile.metrics.tck.metrics;

import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;

import jakarta.inject.Inject;

public class TimerFieldBean {

    @Inject
    private Timer timerWithoutAnnotation;

    @Inject
    @Metric(absolute = false)
    private Timer timerWithExplicitNonAbsoluteName;

    @Inject
    @Metric
    private Timer timerWithNoName;

    @Inject
    @Metric(name = "timerName")
    private Timer timerWithName;

    @Inject
    @Metric(absolute = true)
    private Timer timerWithAbsoluteDefaultName;

    @Inject
    @Metric(name = "timerAbsoluteName", absolute = true)
    private Timer timerWithAbsoluteName;
}
