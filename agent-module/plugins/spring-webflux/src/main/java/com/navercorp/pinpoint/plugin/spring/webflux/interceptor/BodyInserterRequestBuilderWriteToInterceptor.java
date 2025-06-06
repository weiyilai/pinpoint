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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventBlockSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxPluginConfig;
import org.springframework.http.client.reactive.ClientHttpRequest;

/**
 * @author jaehong.kim
 */
public class BodyInserterRequestBuilderWriteToInterceptor extends AsyncContextSpanEventBlockSimpleAroundInterceptor {
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<ClientHttpRequest> cookieRecorder;
    private final RequestTraceWriter<ClientHttpRequest> requestTraceWriter;

    public BodyInserterRequestBuilderWriteToInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final SpringWebFluxPluginConfig config = new SpringWebFluxPluginConfig(traceContext.getProfilerConfig());
        final ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(config.isParam(), clientRequestAdaptor);

        final CookieExtractor<ClientHttpRequest> cookieExtractor = new ClientHttpRequestCookieExtractor();
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(config.getHttpDumpConfig(), cookieExtractor);

        final ClientHttpRequestClientHeaderAdaptor clientHeaderAdaptor = new ClientHttpRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(AsyncContext asyncContext, Trace trace, Object target, Object[] args) {
        final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
        if (request == null) {
            return false;
        }

        if (requestTraceWriter.isNested(request)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            requestTraceWriter.write(request);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
        if (request == null) {
            return;
        }

        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        final ClientRequestWrapper clientRequestWrapper = new WebClientRequestWrapper(request);
        requestTraceWriter.write(request, nextId, clientRequestWrapper.getDestinationId());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX_CLIENT);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
        final ClientRequestWrapper clientRequestWrapper = new WebClientRequestWrapper(request);
        this.clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
        this.cookieRecorder.record(recorder, request, throwable);

        if (isAsync(result)) {
            // make asynchronous trace-id
            final AsyncContext nextAsyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(nextAsyncContext);
            if (isDebug) {
                logger.debug("Set closeable-AsyncContext {}", nextAsyncContext);
            }
        }
    }

    private boolean isAsync(Object result) {
        if (result == null) {
            return false;
        }
        if (!(result instanceof AsyncContextAccessor)) {
            return false;
        }
        return true;
    }
}