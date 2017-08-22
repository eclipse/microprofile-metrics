/*
 * Copyright (C) 2010-2013 Coda Hale, Yammer.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *   2013-04-20 - Coda Hale
 *      Initially authored in dropwizard/metrics SHA:afcf7fd6a12a0f133641
 *   2017-08-17 - Raymond Lam / IBM Corp
 *      Marking as Interface
 */
package org.eclipse.microprofile.metrics;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 *
 */
public interface Meter extends Metered {
 
    /**
     * Mark the occurrence of an event.
     */
    public void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n);

    @Override
    public long getCount();

    @Override
    public double getFifteenMinuteRate();

    @Override
    public double getFiveMinuteRate();

    @Override
    public double getMeanRate();

    @Override
    public double getOneMinuteRate();
}
