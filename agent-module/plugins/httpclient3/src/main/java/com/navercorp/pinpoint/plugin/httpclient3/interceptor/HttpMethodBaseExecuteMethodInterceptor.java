/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.httpclient3.interceptor;

import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseHeaderRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServerResponseHeaderRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContext;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContextFactory;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3PluginConfig;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3ResponseHeaderAdaptor;
import com.navercorp.pinpoint.plugin.httpclient3.HttpMethodClientHeaderAdaptor;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;

import java.util.Objects;

/**
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class HttpMethodBaseExecuteMethodInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final InterceptorScope interceptorScope;
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<HttpMethod> requestTraceWriter;

    private final boolean io;
    private final CookieRecorder<HttpMethod> cookieRecorder;
    private final EntityRecorder<HttpMethod> entityRecorder;
    private final ServerResponseHeaderRecorder<HttpMethod> responseHeaderRecorder;
    private final boolean markError;

    public HttpMethodBaseExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope interceptorScope) {
        super(traceContext, descriptor);
        this.interceptorScope = Objects.requireNonNull(interceptorScope, "interceptorScope");

        final boolean param = HttpClient3PluginConfig.isParam(traceContext.getProfilerConfig());
        final HttpDumpConfig httpDumpConfig = HttpClient3PluginConfig.getHttpDumpConfig(traceContext.getProfilerConfig());

        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(param, clientRequestAdaptor);

        CookieExtractor<HttpMethod> cookieExtractor = HttpClient3CookieExtractor.INSTANCE;
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(httpDumpConfig, cookieExtractor);

        EntityExtractor<HttpMethod> entityExtractor = HttpClient3EntityExtractor.INSTANCE;
        this.entityRecorder = EntityRecorderFactory.newEntityRecorder(httpDumpConfig, entityExtractor);

        this.responseHeaderRecorder = ResponseHeaderRecorderFactory.newResponseHeaderRecorder(traceContext.getProfilerConfig(), new HttpClient3ResponseHeaderAdaptor());

        ClientHeaderAdaptor<HttpMethod> clientHeaderAdaptor = new HttpMethodClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);

        this.io = HttpClient3PluginConfig.isIo(traceContext.getProfilerConfig());
        this.markError = HttpClient3PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof HttpMethod)) {
            return false;
        }

        final HttpMethod httpMethod = (HttpMethod) target;
        if (requestTraceWriter.isNested(httpMethod)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            this.requestTraceWriter.write(httpMethod);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof HttpMethod)) {
            return;
        }
        final HttpMethod httpMethod = (HttpMethod) target;

        // generate next trace id.
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        // set http header for trace.
        final HttpConnection httpConnection = getHttpConnection(args);
        final String host = getHost(httpMethod, httpConnection);
        this.requestTraceWriter.write(httpMethod, nextId, host);
        // init attachment for io(read/write).
        initAttachment();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
    }

    private String getHost(HttpMethod httpMethod, HttpConnection httpConnection) {
        try {
            final URI uri = httpMethod.getURI();
            // if uri have schema
            if (uri.isAbsoluteURI()) {
                return HttpClient3RequestWrapper.getEndpoint(uri.getHost(), uri.getPort());
            }
            if (httpConnection != null) {
                final String host = httpConnection.getHost();
                final int port = HttpClient3RequestWrapper.getPort(httpConnection);
                return HttpClient3RequestWrapper.getEndpoint(host, port);
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("Failed to get host. httpMethod={}", httpMethod, e);
            }
        }
        return null;
    }

    private HttpConnection getHttpConnection(final Object[] args) {
        return ArrayArgumentUtils.getArgument(args, 1, HttpConnection.class);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordServiceType(HttpClient3Constants.HTTP_CLIENT_3);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);

        if (target instanceof HttpMethod) {
            final HttpMethod httpMethod = (HttpMethod) target;
            final HttpConnection httpConnection = getHttpConnection(args);
            final ClientRequestWrapper requestWrapper = new HttpClient3RequestWrapper(httpMethod, httpConnection);
            this.clientRequestRecorder.record(recorder, requestWrapper, throwable);
            this.cookieRecorder.record(recorder, httpMethod, throwable);
            this.entityRecorder.record(recorder, httpMethod, throwable);
            this.responseHeaderRecorder.recordHeader(recorder, httpMethod);
        }

        if (result != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, result);
        }

        final HttpClient3CallContext callContext = getAndCleanAttachment();
        if (callContext != null) {
            recordIo(recorder, callContext);
        }
    }

    private void initAttachment() {
        InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null) {
            invocation.getOrCreateAttachment(HttpClient3CallContextFactory.HTTPCLIENT3_CONTEXT_FACTORY);
        }
    }

    private HttpClient3CallContext getAndCleanAttachment() {
        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        final Object attachment = getAttachment(invocation);
        if (attachment instanceof HttpClient3CallContext) {
            return (HttpClient3CallContext) invocation.removeAttachment();
        }

        return null;
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }

    private void recordIo(SpanEventRecorder recorder, HttpClient3CallContext callContext) {
        if (io) {
            IntBooleanIntBooleanValue value = new IntBooleanIntBooleanValue((int) callContext.getWriteElapsedTime(), callContext.isWriteFail(), (int) callContext.getReadElapsedTime(), callContext.isReadFail());
            recorder.recordAttribute(AnnotationKey.HTTP_IO, value);
        }
    }
}