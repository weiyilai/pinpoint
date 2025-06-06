/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.id;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceRootFactory implements TraceRootFactory {

    private final ObjectName objectName;
    private final TraceIdFactory traceIdFactory;

    @Inject
    public DefaultTraceRootFactory(ObjectName objectName, TraceIdFactory traceIdFactory) {
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.traceIdFactory = Objects.requireNonNull(traceIdFactory, "traceIdFactory");
    }

    @Override
    public TraceRoot newTraceRoot(long transactionId) {
        final TraceId traceId = traceIdFactory.newTraceId(transactionId);
        final long startTime = traceStartTime();
        return TraceRoot.remote(traceId, this.objectName.getAgentId(), startTime, transactionId);
    }


    @Override
    public LocalTraceRoot newDisableTraceRoot(long transactionId) {
        final long startTime = traceStartTime();
        return TraceRoot.local(this.objectName.getAgentId(), startTime, transactionId);
    }

    private long traceStartTime() {
        return System.currentTimeMillis();
    }


    @Override
    public TraceRoot continueTraceRoot(TraceId traceId, long transactionId) {
        Objects.requireNonNull(traceId, "traceId");

        final long startTime = traceStartTime();
        return TraceRoot.remote(traceId, this.objectName.getAgentId(), startTime, transactionId);
    }
}
