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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.DefaultMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@Repository
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<StringMetaDataBo>> stringMetaDataMapper;

    private final HbaseTables.StringMetadataStr DESCRIPTOR = HbaseTables.STRING_METADATA_STR;

    private final RowKeyEncoder<MetaDataRowKey> rowKeyEncoder;

    public HbaseStringMetaDataDao(HbaseOperations hbaseOperations,
                                  @Qualifier("metaDataRowKeyEncoder")
                                  RowKeyEncoder<MetaDataRowKey> rowKeyEncoder,
                                  TableNameProvider tableNameProvider,
                                  @Qualifier("stringMetaDataMapper")
                                  RowMapper<List<StringMetaDataBo>> stringMetaDataMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.stringMetaDataMapper = Objects.requireNonNull(stringMetaDataMapper, "stringMetaDataMapper");
    }

    @Override
    public List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId) {
        Objects.requireNonNull(agentId, "agentId");

        MetaDataRowKey metaDataRowKey = new DefaultMetaDataRowKey(agentId, time, stringId);
        byte[] rowKey = rowKeyEncoder.encodeRowKey(metaDataRowKey);

        Get get = new Get(rowKey);
        get.addFamily(DESCRIPTOR.getName());

        TableName stringMetaDataTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.get(stringMetaDataTableName, get, stringMetaDataMapper);
    }

}
