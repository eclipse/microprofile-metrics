/*
 **********************************************************************
 * Copyright (c) 2017, 2024 Contributors to the Eclipse Foundation
 *               2010-2013 Coda Hale, Yammer.com
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
package org.eclipse.microprofile.metrics;

import java.io.OutputStream;

/**
 * A statistical snapshot of a {@link Snapshot}.
 */
public abstract class Snapshot {

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values
     */
    public abstract long size();

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    public abstract double getMax();

    /**
     * Returns the arithmetic mean of the values in the snapshot.
     *
     * @return the arithmetic mean
     */
    public abstract double getMean();

    /**
     * Returns an array of {@link PercentileValue} containing the percentiles and associated values of this
     * {@link Snapshot} at the moment invocation.
     *
     * @return an array of {@link PercentileValue} if it is available or an empty array if not available
     */
    public abstract PercentileValue[] percentileValues();

    /**
     * Returns an array of {@link HistogramBucket} containing the bucket and associated value of this {@link Snapshot}
     * at the moment invocation.
     *
     * @return an array of {@link HistogramBucket} if it is available or an empty array if not available
     */
    @aQute.bnd.annotation.baseline.BaselineIgnore("5.2")
    public HistogramBucket[] bucketValues() {
        return new HistogramBucket[0];
    };

    /**
     * Writes the values of the snapshot to the given stream.
     *
     * @param output
     *            an output stream
     */
    public abstract void dump(OutputStream output);

    /**
     * Represents a percentile and its value at the moment it was sampled from the Snapshot. Percentile values of a
     * {@link Timer} are represented in units of nanoseconds.
     *
     * See {@link Snapshot#percentileValues()}
     */
    public static class PercentileValue {
        private final double percentile;
        private final double value;

        /**
         *
         * @param percentile
         *            percentile
         * @param value
         *            value of percentile
         */
        public PercentileValue(double percentile, double value) {
            this.percentile = percentile;
            this.value = value;
        }

        /**
         * Returns the percentile
         *
         * @return the percentile
         */
        public double getPercentile() {
            return this.percentile;
        }

        /**
         * Returns the value at percentile
         *
         * @return the value at the percentile
         */
        public double getValue() {
            return this.value;
        }

        public String toString() {
            return "[Percentile: " + (this.percentile * 100.0) + "% with Value: " + this.value + "]";
        }

    }

    /**
     * Represents a cumulative histogram bucket at the moment it was sampled from the Snapshot. The bucket of
     * {@link Timer} will be represented in nanoseconds.
     *
     * See {@link #bucketValues()}
     */
    public static class HistogramBucket {
        private final double bucket;
        private final long count;

        /**
         *
         * @param count
         *            count at this bucket
         * @param bucket
         *            the upper limit value of this bucket
         */
        public HistogramBucket(double bucket, long count) {
            this.bucket = bucket;
            this.count = count;
        }

        /**
         * Returns the count of the bucket
         *
         * @return the count of the bucket
         */
        public long getCount() {
            return this.count;
        }

        /**
         * Returns the upper limit value of this bucket
         *
         * @return the upper limit value of this bucket
         */
        public double getBucket() {
            return this.bucket;
        }

        public String toString() {
            return "[Bucket: " + (this.bucket) + " with count: " + this.count + "]";
        }

    }
}
