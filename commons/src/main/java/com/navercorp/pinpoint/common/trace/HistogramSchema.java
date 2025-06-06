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

package com.navercorp.pinpoint.common.trace;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public interface HistogramSchema {
    Schema getSchema();

    int getTypeCode();

    /**
     * find the most appropriate slot based on elapsedTime
     *
     * @param elapsedTime
     * @return
     */
    HistogramSlot findHistogramSlot(int elapsedTime, boolean error);

    HistogramSlot getFastSlot();

    HistogramSlot getNormalSlot();

    HistogramSlot getSlowSlot();

    HistogramSlot getVerySlowSlot();

    // error ----------
    
    HistogramSlot getTotalErrorView();

    HistogramSlot getFastErrorSlot();

    HistogramSlot getNormalErrorSlot();

    HistogramSlot getSlowErrorSlot();

    HistogramSlot getVerySlowErrorSlot();

    HistogramSlot getSumStatSlot();

    HistogramSlot getMaxStatSlot();

    HistogramSlot getPingSlot();
}