package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import org.apache.hadoop.hbase.client.Put;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkSerializerV2 implements HbaseSerializer<SpanChunkBo, Put> {

    private final SpanEncoder spanEncoder;

    public SpanChunkSerializerV2(SpanEncoder spanEncoder) {
        this.spanEncoder = Objects.requireNonNull(spanEncoder, "spanEncoder");
    }

    @Override
    public void serialize(SpanChunkBo spanChunkBo, Put put, SerializationContext context) {
        Objects.requireNonNull(spanChunkBo, "spanChunkBo");


        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<>(spanChunkBo);

        ByteBuffer qualifier = spanEncoder.encodeSpanChunkQualifier(encodingContext);
        ByteBuffer columnValue = spanEncoder.encodeSpanChunkColumnValue(encodingContext);

        long acceptedTime = put.getTimestamp();
        put.addColumn(HbaseTables.TRACE_V2_SPAN.getName(), qualifier, acceptedTime, columnValue);
    }

}
