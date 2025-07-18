/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.controller.FilteredMapController;
import com.navercorp.pinpoint.web.applicationmap.controller.MapController;
import com.navercorp.pinpoint.web.applicationmap.controller.MapHistogramController;
import com.navercorp.pinpoint.web.applicationmap.controller.ServerMapHistogramController;
import com.navercorp.pinpoint.web.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.map.ApplicationsMapCreatorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.processor.ApplicationLimiterProcessorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.service.FilteredMapService;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.LinkDataMapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.task.RequestContextPropagatingTaskDecorator;
import com.navercorp.pinpoint.web.task.SecurityContextPropagatingTaskDecorator;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.applicationmap.service",
})
@Import(MapHbaseConfiguration.class)
public class ApplicationMapModule {
    private static final Logger logger = LogManager.getLogger(ApplicationMapModule.class);

    public ApplicationMapModule() {
        logger.info("Install {}", ApplicationMapModule.class.getSimpleName());
    }

    @Bean
    public MapController mapController(MapService mapService,
                                       ApplicationValidator applicationValidator,
                                       HyperLinkFactory hyperLinkFactory,
                                       ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new MapController(mapService, applicationValidator, hyperLinkFactory, maxPeriod);
    }

    @Bean
    public MapHistogramController mapHistogramController(ResponseTimeHistogramService responseTimeHistogramService,
                                                         HistogramService histogramService,
                                                         ApplicationFactory applicationFactory,
                                                         ApplicationValidator applicationValidator,
                                                         HyperLinkFactory hyperLinkFactory,
                                                         ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new MapHistogramController(responseTimeHistogramService, histogramService, applicationFactory, applicationValidator, hyperLinkFactory, maxPeriod);
    }

    @Bean
    public ServerMapHistogramController serverMapHistogramController(ResponseTimeHistogramService responseTimeHistogramService,
                                                                   HistogramService histogramService,
                                                                   ApplicationFactory applicationFactory,
                                                                   ApplicationValidator applicationValidator,
                                                                   HyperLinkFactory hyperLinkFactory,
                                                                   ConfigProperties configProperties) {
        Duration maxPeriod = Duration.ofDays(configProperties.getServerMapPeriodMax());
        return new ServerMapHistogramController(responseTimeHistogramService, histogramService, applicationFactory, applicationValidator, hyperLinkFactory, maxPeriod);
    }

    @Bean
    public FilteredMapController filteredMapController(FilteredMapService filteredMapService,
                                                       FilterBuilder<List<SpanBo>> filterBuilder,
                                                       HyperLinkFactory hyperLinkFactory) {
        return new FilteredMapController(filteredMapService, filterBuilder, hyperLinkFactory);
    }

    @Bean
    public ApplicationMapBuilderFactory applicationMapBuilderFactory(
            NodeHistogramAppenderFactory nodeHistogramAppenderFactory,
            ServerInfoAppenderFactory serverInfoAppenderFactory) {
        return new ApplicationMapBuilderFactory(nodeHistogramAppenderFactory, serverInfoAppenderFactory);
    }

    @Bean
    public NodeHistogramAppenderFactory nodeHistogramAppenderFactory(@Qualifier("nodeHistogramAppendExecutor") Executor executor) {
        return new NodeHistogramAppenderFactory(executor);
    }

    @Bean
    public ServerInfoAppenderFactory serverInfoAppenderFactory(@Qualifier("serverInfoAppendExecutor") Executor executor) {
        return new ServerInfoAppenderFactory(executor);
    }

    @Bean
    public ApplicationsMapCreatorFactory applicationsMapCreatorFactory(@Qualifier("applicationsMapCreateExecutor") Executor executor) {
        return new ApplicationsMapCreatorFactory(executor);
    }

    @Bean
    public Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory(@Value("${pinpoint.server-map.read-limit:200}") int limit) {
        return new ApplicationLimiterProcessorFactory(limit);
    }

    @Bean
    public LinkSelectorFactory linkSelectorFactory(LinkDataMapService linkDataMapService,
                                                   ApplicationsMapCreatorFactory applicationsMapCreatorFactory,
                                                   HostApplicationMapDao hostApplicationMapDao,
                                                   Optional<ServerMapDataFilter> serverMapDataFilter,
                                                   Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory) {
        return new LinkSelectorFactory(linkDataMapService, applicationsMapCreatorFactory, hostApplicationMapDao, serverMapDataFilter, applicationLimiterProcessorFactory);
    }

    @Bean
    @Validated
    @ConfigurationProperties("web.servermap.creator.worker")
    public ExecutorProperties creatorExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public Executor applicationsMapCreateExecutor(@Qualifier("creatorExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }

    @Bean
    @Validated
    @ConfigurationProperties("web.servermap.appender.worker")
    public ExecutorProperties appenderExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public Executor nodeHistogramAppendExecutor(@Qualifier("appenderExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }

    @Bean
    public Executor serverInfoAppendExecutor(@Qualifier("appenderExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }


    public TaskDecorator contextPropagatingTaskDecorator() {
        TaskDecorator requestDecorator = new RequestContextPropagatingTaskDecorator();
        TaskDecorator securityDecorator = new SecurityContextPropagatingTaskDecorator();
        return new CompositeTaskDecorator(List.of(requestDecorator, securityDecorator));
    }

    @Bean
    public ExecutorCustomizer<ThreadPoolTaskExecutor> executorCustomizer() {
        TaskDecorator taskDecorator = contextPropagatingTaskDecorator();
        return new TaskExecutorCustomizer(taskDecorator);
    }

}
