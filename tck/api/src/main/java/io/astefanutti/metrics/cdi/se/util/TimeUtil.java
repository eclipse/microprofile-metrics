/**
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
package io.astefanutti.metrics.cdi.se.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeUtil {

    private TimeUtil() {

    }

    /**
     * Wait until a new minute starts (wait for a timestamp ending with :00.000) and then continue
     */
    public static void waitForNextMinute() throws InterruptedException, TimeoutException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timer t = new Timer();
        final CountDownLatch done = new CountDownLatch(1); // marks the actual start of the new minute
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                done.countDown();
            }
        };
        // In the rare case when a new minute started between the creation of the original 'calendar' and this line,
        // then the timer's target time is actually in the past, but java.util.Timer, according to its javadoc,
        // should fire immediately in this case, so we should be fine and just continue.
        t.schedule(task, calendar.getTime());
        if(!done.await(65, TimeUnit.SECONDS)) {
            throw new TimeoutException();
        }
    }

}
