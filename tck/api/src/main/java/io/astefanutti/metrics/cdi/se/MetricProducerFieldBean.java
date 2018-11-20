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

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metric;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MetricProducerFieldBean {

    @Produces
    @Metric(name = "counter1", absolute = true)
    private final Counter counter1 = new SimpleCounter() {

    };

    @Produces
    @Metric(name = "counter2", absolute = true)
    private final Counter counter2 = new SimpleCounter();

    @Produces
    @Metric(name = "ratioGauge", absolute = true)
    private final Gauge<Double> gauge = new Gauge<Double>() {
        public Double getValue() {
            return (double) counter1.getCount() / (double) counter2.getCount();
        };
    };

    @Produces
    @FooQualifier
    @Metric(name = "not_registered_metric", absolute = true)
    private final Counter not_registered_metric = new SimpleCounter();

    class SimpleCounter implements Counter {

        private long count;

        @Override
        public void inc() {
            count++;
        }

        @Override
        public void inc(long n) {
            count += n;
        }

        @Override
        public long getCount() {
            return count;
        }
    }
}
