/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Taejin Koo
 */
public class HbaseTableTest {

    @Test
    public void agentInfoInfoTest() {
        HbaseTables.AgentInfo agentinfoInfo = HbaseTables.AGENTINFO_INFO;
        Assertions.assertArrayEquals(Bytes.toBytes("Info"), agentinfoInfo.getName());
        Assertions.assertEquals("AgentInfo", agentinfoInfo.getTable().getName());
        Assertions.assertArrayEquals(Bytes.toBytes("m"), agentinfoInfo.QUALIFIER_SERVER_META_DATA);
        Assertions.assertArrayEquals(Bytes.toBytes("i"), agentinfoInfo.QUALIFIER_IDENTIFIER);
        Assertions.assertArrayEquals(Bytes.toBytes("j"), agentinfoInfo.QUALIFIER_JVM);
    }

    @Test
    public void agentEventEventsTest() {
        HbaseColumnFamily agentEventEvents = HbaseTables.AGENT_EVENT_EVENTS;
        Assertions.assertArrayEquals(Bytes.toBytes("E"), agentEventEvents.getName());
        Assertions.assertEquals("AgentEvent", agentEventEvents.getTable().getName());
    }

    @Test
    public void agentLifecycleStatusTest() {
        HbaseTables.AgentLifeCycleStatus agentLifecycleStatus = HbaseTables.AGENT_LIFECYCLE_STATUS;
        Assertions.assertArrayEquals(Bytes.toBytes("S"), agentLifecycleStatus.getName());
        Assertions.assertEquals("AgentLifeCycle", agentLifecycleStatus.getTable().getName());
        Assertions.assertArrayEquals(Bytes.toBytes("states"), agentLifecycleStatus.QUALIFIER_STATES);
    }

    @Test
    public void apiMetadataApiTest() {
        HbaseTables.ApiMetadata apiMetadataApi = HbaseTables.API_METADATA_API;
        Assertions.assertArrayEquals(Bytes.toBytes("Api"), apiMetadataApi.getName());
        Assertions.assertEquals("ApiMetaData", apiMetadataApi.getTable().getName());
        Assertions.assertArrayEquals(Bytes.toBytes("P_api_signature"), apiMetadataApi.QUALIFIER_SIGNATURE);
    }

    @Test
    public void applicationIndexAgentsTest() {
        HbaseColumnFamily applicationIndexAgents = HbaseTables.APPLICATION_INDEX_AGENTS;
        Assertions.assertArrayEquals(Bytes.toBytes("Agents"), applicationIndexAgents.getName());
        Assertions.assertEquals("ApplicationIndex", applicationIndexAgents.getTable().getName());
    }

    @Test
    public void applicationTraceIndexTraceTest() {
        HbaseTables.ApplicationTraceIndexTrace applicationTraceIndexTrace = HbaseTables.APPLICATION_TRACE_INDEX_TRACE;
        Assertions.assertArrayEquals(Bytes.toBytes("I"), applicationTraceIndexTrace.getName());
        Assertions.assertEquals("ApplicationTraceIndex", applicationTraceIndexTrace.getTable().getName());
        Assertions.assertEquals(1, applicationTraceIndexTrace.ROW_DISTRIBUTE_SIZE);
    }

    @Test
    public void hostApplicationMapVer2MapTest() {
        HbaseColumnFamily hostApplicationMapVer2Map = HbaseTables.HOST_APPLICATION_MAP_VER2_MAP;
        Assertions.assertArrayEquals(Bytes.toBytes("M"), hostApplicationMapVer2Map.getName());
        Assertions.assertEquals("HostApplicationMap_Ver2", hostApplicationMapVer2Map.getTable().getName());
    }

    @Test
    public void mapStatisticsCalleeVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCalleeVer2Counter = HbaseTables.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        Assertions.assertArrayEquals(Bytes.toBytes("C"), mapStatisticsCalleeVer2Counter.getName());
        Assertions.assertEquals("ApplicationMapStatisticsCallee_Ver2", mapStatisticsCalleeVer2Counter.getTable().getName());
    }

    @Test
    public void mapStatisticsCallerVer2CounterTest() {
        HbaseColumnFamily mapStatisticsCallerVer2Counter = HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER;
        Assertions.assertArrayEquals(Bytes.toBytes("C"), mapStatisticsCallerVer2Counter.getName());
        Assertions.assertEquals("ApplicationMapStatisticsCaller_Ver2", mapStatisticsCallerVer2Counter.getTable().getName());
    }

    @Test
    public void mapStatisticsSelfVer2CounterTest() {
        HbaseColumnFamily mapStatisticsSelfVer2Counter = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;
        Assertions.assertArrayEquals(Bytes.toBytes("C"), mapStatisticsSelfVer2Counter.getName());
        Assertions.assertEquals("ApplicationMapStatisticsSelf_Ver2", mapStatisticsSelfVer2Counter.getTable().getName());
    }

    @Test
    public void sqlMetadataVer2SqlTest() {
        HbaseTables.SqlMetadataV2 sqlMetadataVer2Sql = HbaseTables.SQL_METADATA_VER2_SQL;
        Assertions.assertArrayEquals(Bytes.toBytes("Sql"), sqlMetadataVer2Sql.getName());
        Assertions.assertEquals("SqlMetaData_Ver2", sqlMetadataVer2Sql.getTable().getName());
        Assertions.assertArrayEquals(Bytes.toBytes("P_sql_statement"), sqlMetadataVer2Sql.QUALIFIER_SQLSTATEMENT);
    }

    @Test
    public void stringMetadataStrTest() {
        HbaseTables.StringMetadataStr stringMetadataStr = HbaseTables.STRING_METADATA_STR;
        Assertions.assertArrayEquals(Bytes.toBytes("Str"), stringMetadataStr.getName());
        Assertions.assertEquals("StringMetaData", stringMetadataStr.getTable().getName());
        Assertions.assertArrayEquals(Bytes.toBytes("P_string"), stringMetadataStr.QUALIFIER_STRING);
    }

    @Test
    public void traceV2SpanTest() {
        HbaseColumnFamily traceV2Span = HbaseTables.TRACE_V2_SPAN;
        Assertions.assertArrayEquals(Bytes.toBytes("S"), traceV2Span.getName());
        Assertions.assertEquals("TraceV2", traceV2Span.getTable().getName());
    }

}
