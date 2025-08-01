package com.navercorp.pinpoint.uid;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.uid.utils.UidBytesCreateUtils;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UidBytesUtilsTest {

    private final ServiceUid testServiceUid = ServiceUid.of(9991);
    private final ApplicationUid testApplicationUid = ApplicationUid.of(9992);
    private final String testApplicationName = "testApplication";
    private final int testServiceType = ServiceType.TEST.getCode();
    private final ApplicationUidAttribute testApplicationUidAttribute = new ApplicationUidAttribute(testApplicationName, testServiceType);

    @Test
    public void commonScanRowKeyTest() {
        byte[] rowKey1 = UidBytesCreateUtils.createRowKey(testServiceUid);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(rowKey1)).isEqualTo(testServiceUid);

        byte[] rowKey2 = UidBytesCreateUtils.createRowKey(testServiceUid, testApplicationUid);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(rowKey2)).isEqualTo(testServiceUid);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidFromRowKey(rowKey2)).isEqualTo(testApplicationUid);

        byte[] rowKey3 = UidBytesCreateUtils.createRowKey(testServiceUid, testApplicationName);
        String applicationUid = BytesUtils.toString(rowKey3, ByteArrayUtils.INT_BYTE_LENGTH, rowKey3.length - ByteArrayUtils.INT_BYTE_LENGTH);
        Assertions.assertThat(applicationUid).isEqualTo(testApplicationName);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(rowKey3)).isEqualTo(testServiceUid);
    }

    @Test
    public void applicationUidTableTest() {
        byte[] applicationUidRowKey = UidBytesCreateUtils.createApplicationUidRowKey(testServiceUid, testApplicationName, testServiceType);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidAttrFromRowKey(applicationUidRowKey)).isEqualTo(testApplicationUidAttribute);

        byte[] value = UidBytesCreateUtils.createApplicationUidValue(testApplicationUid);
        long applicationUidCode = ByteArrayUtils.bytesToLong(value, 0);
        Assertions.assertThat(applicationUidCode).isEqualTo(testApplicationUid.getUid());
    }

    @Test
    public void applicationUidAttrTableTest() {
        byte[] applicationUidAttrRowKey = UidBytesCreateUtils.createRowKey(testServiceUid, testApplicationUid);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(applicationUidAttrRowKey)).isEqualTo(testServiceUid);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidFromRowKey(applicationUidAttrRowKey)).isEqualTo(testApplicationUid);

        byte[] value = UidBytesCreateUtils.createApplicationUidAttrValue(testApplicationName, testServiceType);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidAttrFromValue(value, 0, value.length)).isEqualTo(testApplicationUidAttribute);
    }

    @Test
    public void agentNameTableTest() {
        final String testAgentId = "AZc_MJNFcFCM3EiUUp_SvQ";
        final String testAgentName = "testAgentName";
        final long testAgentStartTime = 1749111831629L;

        byte[] agentNameRowKey = UidBytesCreateUtils.createAgentNameRowKey(testServiceUid, testApplicationUid, testAgentId, testAgentStartTime);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(agentNameRowKey)).isEqualTo(testServiceUid);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidFromRowKey(agentNameRowKey)).isEqualTo(testApplicationUid);
        Assertions.assertThat(UidBytesParseUtils.parseAgentId(agentNameRowKey)).isEqualTo(testAgentId);
        Assertions.assertThat(UidBytesParseUtils.parseAgentStartTime(agentNameRowKey)).isEqualTo(testAgentStartTime);

        byte[] agentNameScanRowKey = UidBytesCreateUtils.createAgentNameRowKey(testServiceUid, testApplicationUid, testAgentId);
        Assertions.assertThat(UidBytesParseUtils.parseServiceUidFromRowKey(agentNameScanRowKey)).isEqualTo(testServiceUid);
        Assertions.assertThat(UidBytesParseUtils.parseApplicationUidFromRowKey(agentNameScanRowKey)).isEqualTo(testApplicationUid);
        Assertions.assertThat(UidBytesParseUtils.parseAgentId(agentNameScanRowKey)).isEqualTo(testAgentId);
    }
}
