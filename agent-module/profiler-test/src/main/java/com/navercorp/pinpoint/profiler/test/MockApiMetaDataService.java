/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.test;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
import com.navercorp.pinpoint.profiler.cache.Result;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class MockApiMetaDataService implements ApiMetaDataService {

    private final SimpleCache<String, Integer> apiCache = SimpleCache.newIdCache();

    private final DataConsumer<MetaDataType> dataSender;

    public MockApiMetaDataService(DataConsumer<MetaDataType> dataSender) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
    }

    @Override
    public int cacheApi(final MethodDescriptor methodDescriptor) {
        final String fullName = methodDescriptor.getFullName();
        final Result<Integer> result = this.apiCache.put(fullName);

        methodDescriptor.setApiId(result.getId());

        final ApiMetaData apiMetadata = new ApiMetaData(result.getId(), methodDescriptor.getApiDescriptor(),
                methodDescriptor.getLineNumber(), methodDescriptor.getType());

        this.dataSender.send(apiMetadata);

        return result.getId();
    }

}

