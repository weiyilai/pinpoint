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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author emeroad
 */
public class HistogramSerializerTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper objectMapper = Jackson.newMapper();


    @Test
    public void testSerialize() throws Exception {
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        HistogramSchema schema = original.getHistogramSchema();
        original.addCallCount(schema.getFastSlot().getSlotTime(), 1);
        original.addCallCount(schema.getNormalSlot().getSlotTime(), 2);
        original.addCallCount(schema.getSlowSlot().getSlotTime(), 3);
        original.addCallCount(schema.getVerySlowSlot().getSlotTime(), 4);
        original.addCallCount(schema.getNormalErrorSlot().getSlotTime(), 5);

        String jacksonJson = objectMapper.writeValueAsString(original);
        Map<String, Object> objectMapperHashMap = objectMapper.readValue(jacksonJson, TypeRef.map());

        logger.debug("{}", jacksonJson);

        Map<String, Object> hashMap = internalJson(original);

        Assertions.assertEquals(objectMapperHashMap, hashMap);
    }

    /**
     * moved this testcase for testing the old version histogram with manually created json code
     */
    public Map<String, Object> internalJson(Histogram histogram) {
        HistogramSchema histogramSchema = histogram.getHistogramSchema();

        return Map.ofEntries(entry(histogramSchema.getFastSlot(), histogram.getFastCount()),
                entry(histogramSchema.getNormalSlot(), histogram.getNormalCount()),
                entry(histogramSchema.getSlowSlot(), histogram.getSlowCount()),
                entry(histogramSchema.getVerySlowSlot(), histogram.getVerySlowCount()),
                entry(histogramSchema.getTotalErrorView(), histogram.getTotalErrorCount())
        );
    }

    private Map.Entry<String, Integer> entry(HistogramSlot histogramSchema, long histogram) {
        return Map.entry(histogramSchema.getSlotName(), (int) histogram);
    }

}
