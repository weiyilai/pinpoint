/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author minwoo-jung
 */
@Component
public class HeatmapProperties {

    @Value("${kafka.heatmap.topic.prefix}")
    private String heatmapTopicPrefix;
    @Value("${kafka.heatmap.topic.padding.length}")
    private int heatMapTopicPaddingLength;
    @Value("${kafka.heatmap.topic.count}")
    private int heatmapTopicCount;


    public String getHeatmapTopicPrefix() {
        return heatmapTopicPrefix;
    }

    public int getHeatMapTopicPaddingLength() {
        return heatMapTopicPaddingLength;
    }

    public int getHeatmapTopicCount() {
        return heatmapTopicCount;
    }
}
