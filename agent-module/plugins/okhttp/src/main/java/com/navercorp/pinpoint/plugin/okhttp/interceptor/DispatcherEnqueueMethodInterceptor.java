/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.okhttp.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;

/**
 * @author jaehong.kim
 */
public class DispatcherEnqueueMethodInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final boolean markError;

    public DispatcherEnqueueMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.markError = OkHttpPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        AsyncContextAccessor accessor = getAsyncContextAccessor(args);
        if (accessor == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        try {
            // set asynchronous trace
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            // AsyncTraceIdAccessor typeCheck validate();
            accessor._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private AsyncContextAccessor getAsyncContextAccessor(Object[] args) {
        AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 0, AsyncContextAccessor.class);
        if (asyncContextAccessor == null) {
            if (isDebug) {
                logger.debug("Invalid args[0] object {}. Need field accessor({}).", args, AsyncContextAccessor.class.getName());
            }
        }

        return asyncContextAccessor;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        AsyncContextAccessor accessor = getAsyncContextAccessor(args);
        if (accessor == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (trace.canSampled()) {
                recorder.recordApi(methodDescriptor);
                recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL);
                recorder.recordException(markError, throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}