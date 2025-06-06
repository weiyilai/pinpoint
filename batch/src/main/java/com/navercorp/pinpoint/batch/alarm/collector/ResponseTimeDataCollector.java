/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class ResponseTimeDataCollector extends DataCollector {

    private final Application application;
    private final MapResponseDao responseDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init =new AtomicBoolean(false); // need to consider a race condition when checkers start simultaneously.

    private long fastCount = 0;
    private long normalCount = 0;
    private long slowCount = 0;
    private long errorCount = 0;
    private long totalCount = 0;
    private long slowRate = 0;
    private long errorRate = 0;

    public ResponseTimeDataCollector(DataCollectorCategory category,
                                     Application application,
                                     MapResponseDao responseDao,
                                     long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = Objects.requireNonNull(application, "application");
        this.responseDao = Objects.requireNonNull(responseDao, "responseDao");
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range range = Range.between(timeSlotEndTime - slotInterval, timeSlotEndTime);
        TimeWindow timeWindow = new TimeWindow(range);

        ApplicationResponse applicationResponse = responseDao.selectApplicationResponse(application, timeWindow);
        sum(applicationResponse.getApplicationHistograms());


        setSlowRate();
        setErrorRate();

        init.set(true);
    }

    private void setSlowRate() {
        slowRate = calculatePercent(slowCount);
    }

    private void setErrorRate() {
        errorRate = calculatePercent(errorCount);
    }

    private long calculatePercent(long value) {
        if (totalCount == 0 || value == 0) {
            return 0;
        } else {
            return (value * 100L) / totalCount;
        }
    }

    private void sum(Collection<TimeHistogram> timeHistograms) {
        for (TimeHistogram timeHistogram : timeHistograms) {
            fastCount += timeHistogram.getFastCount();
            normalCount += timeHistogram.getNormalCount();
            slowCount += timeHistogram.getSlowCount();
            slowCount += timeHistogram.getVerySlowCount();
            errorCount += timeHistogram.getTotalErrorCount();
            totalCount += timeHistogram.getTotalCount();
        }
    }

    public long getFastCount() {
        return fastCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getSlowRate() {
        return slowRate;
    }

    public long getErrorRate() {
        return errorRate;
    }

}
