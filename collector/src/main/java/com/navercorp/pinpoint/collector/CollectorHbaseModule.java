package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.collector.config.BatchHbaseClientConfiguration;
import com.navercorp.pinpoint.collector.config.HbaseAsyncConfiguration;
import com.navercorp.pinpoint.collector.config.SchedulerConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.encode.ApplicationIndexRowKeyEncoderV1;
import com.navercorp.pinpoint.collector.dao.hbase.encode.ApplicationIndexRowKeyEncoderV2;
import com.navercorp.pinpoint.collector.util.DurabilityApplier;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbasePutWriterConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.hbase.config.HbaseClientConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
        CommonsHbaseConfiguration.class,
        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,

        HbaseClientConfiguration.class,
        HbaseTemplateConfiguration.class,
        HbasePutWriterConfiguration.class,

        BatchHbaseClientConfiguration.class,

        HbaseAsyncConfiguration.class,
        SchedulerConfiguration.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.dao.hbase"
})
@PropertySource(name = "CollectorHbaseModule", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:local}/hbase.properties"
})
public class CollectorHbaseModule {
    private final Logger logger = LogManager.getLogger(CollectorHbaseModule.class);

    @Bean("applicationIndexRowKeyEncoder")
    @ConditionalOnProperty(name = "collector.scatter.serverside-scan", havingValue = "v1")
    public RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoderV1(@Qualifier("applicationTraceIndexDistributor")
                                                                 RowKeyDistributor rowKeyDistributor) {
        return new ApplicationIndexRowKeyEncoderV1(rowKeyDistributor);
    }

    @Bean("applicationIndexRowKeyEncoder")
    @ConditionalOnProperty(name = "collector.scatter.serverside-scan", havingValue = "v2", matchIfMissing = true)
    public RowKeyEncoder<SpanBo> applicationIndexRowKeyEncoderV2(@Qualifier("applicationTraceIndexDistributor")
                                                                 RowKeyDistributor rowKeyDistributor) {
        return new ApplicationIndexRowKeyEncoderV2(rowKeyDistributor);
    }

    @Bean
    public DurabilityApplier spanPutWriterDurabilityApplier(@Value("${collector.span.durability:USE_DEFAULT}") String spanDurability) {
        logger.info("Span(Trace Put) durability:{}", spanDurability);
        return new DurabilityApplier(spanDurability);
    }

}
