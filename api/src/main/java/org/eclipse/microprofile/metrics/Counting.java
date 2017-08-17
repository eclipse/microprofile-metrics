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
 *   2013-05-23 - Coda Hale
 *      Initially authored in dropwizard/metrics SHA:8ec4235cbda58adb4e75
 */
package org.eclipse.microprofile.metrics;

/**
 * An interface for metric types which have counts.
 */
public interface Counting {
    /**
     * Returns the current count.
     *
     * @return the current count
     */
    long getCount();
}
