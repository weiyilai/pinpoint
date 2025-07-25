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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.StringMetaDataDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Repository
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseTables.StringMetadataStr DESCRIPTOR = HbaseTables.STRING_METADATA_STR;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;


    private final RowKeyEncoder<MetaDataRowKey> rowKeyEncoder;

    public HbaseStringMetaDataDao(HbaseOperations hbaseTemplate,
                                  @Qualifier("metaDataRowKeyEncoder")
                                  RowKeyEncoder<MetaDataRowKey> rowKeyEncoder,
                                  TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(StringMetaDataBo stringMetaData) {
        Objects.requireNonNull(stringMetaData, "stringMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", stringMetaData);
        }

        byte[] rowKey = rowKeyEncoder.encodeRowKey(stringMetaData);

        final Put put = new Put(rowKey, true);
        final String stringValue = stringMetaData.getStringValue();
        final byte[] sqlBytes = Bytes.toBytes(stringValue);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_STRING, sqlBytes);

        final TableName stringMetaDataTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(stringMetaDataTableName, put);
    }
}
