/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class FixedBufferTest {
    public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Random random = new Random();

    @Test
    public void testPutPrefixedBytes() {
        String test = "test";
        int endExpected = 3333;
        testPutPrefixedBytes(test, endExpected);
        testPutPrefixedBytes(null, endExpected);
        testPutPrefixedBytes("", endExpected);
    }

    private void testPutPrefixedBytes(String test, int expected) {
        Buffer buffer = new FixedBuffer(1024);
        if (test != null) {
            buffer.putPrefixedBytes(test.getBytes(UTF8_CHARSET));
        } else {
            buffer.putPrefixedString(null);
        }

        buffer.putInt(expected);
        byte[] buffer1 = buffer.getBuffer();

        Buffer actual = new FixedBuffer(buffer1);
        String s = actual.readPrefixedString();
        Assertions.assertEquals(test, s);

        int i = actual.readInt();
        Assertions.assertEquals(expected, i);
    }

    @Test
    public void testPadBytes() {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        Buffer buffer = new FixedBuffer(32);
        byte[] test = new byte[10];

        random.nextBytes(test);

        buffer.putPadBytes(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        assertThat(result).hasSize(TOTAL_LENGTH);
        Assertions.assertArrayEquals(Arrays.copyOfRange(test, 0, TEST_SIZE), Arrays.copyOfRange(result, 0, TEST_SIZE), "check data");
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        Assertions.assertArrayEquals(Arrays.copyOfRange(padBytes, 0, TEST_SIZE), Arrays.copyOfRange(result, TEST_SIZE, TOTAL_LENGTH), "check pad");

    }

    @Test
    public void readPadBytes() {
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        Buffer writeBuffer = new FixedBuffer(32);
        writeBuffer.putPadBytes(bytes, 20);
        writeBuffer.putInt(255);

        Buffer readBuffer = new FixedBuffer(writeBuffer.getBuffer());
        byte[] readPadBytes = readBuffer.readPadBytes(20);
        Assertions.assertArrayEquals(bytes, Arrays.copyOf(readPadBytes, 10));
        int readInt = readBuffer.readInt();
        Assertions.assertEquals(255, readInt);
    }


    @Test
    public void testPadBytes_Error() {

        Buffer buffer1_1 = new FixedBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_1.putPadBytes(new byte[11], 10);
        });

        Buffer buffer1_2 = new FixedBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_2.putPadBytes(new byte[20], 10);
        });

        Buffer buffer2 = new FixedBuffer(32);
        buffer2.putPadBytes(new byte[10], 10);

    }

    @Test
    public void testPadString() {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        int PAD_SIZE = TOTAL_LENGTH - TEST_SIZE;
        Buffer buffer = new FixedBuffer(32);
        String test = StringUtils.repeat("a", TEST_SIZE);

        buffer.putPadString(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        String decodedString = new String(result);
        String trimString = decodedString.trim();
        assertThat(result).hasSize(TOTAL_LENGTH);

        Assertions.assertEquals(test, trimString, "check data");

        String padString = new String(result, TOTAL_LENGTH - TEST_SIZE, PAD_SIZE, UTF8_CHARSET);
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        Assertions.assertEquals(padString, new String(padBytes, UTF8_CHARSET), "check pad");

    }

    @Test
    public void readPadString() {
        String testString = StringUtils.repeat("a", 10);
        Buffer writeBuffer = new FixedBuffer(32);
        writeBuffer.putPadString(testString, 20);
        writeBuffer.putInt(255);

        Buffer readBuffer = new FixedBuffer(writeBuffer.getBuffer());
        String readPadString = readBuffer.readPadString(20);
        Assertions.assertEquals(testString, readPadString.substring(0, 10));
        int readInt = readBuffer.readInt();
        Assertions.assertEquals(255, readInt);
    }

    @Test
    public void readPadStringAndRightTrim() {
        String testString = StringUtils.repeat("a", 10);
        Buffer writeBuffer = new FixedBuffer(32);
        writeBuffer.putPadString(testString, 20);
        writeBuffer.putInt(255);

        Buffer readBuffer = new FixedBuffer(writeBuffer.getBuffer());
        String readPadString = readBuffer.readPadStringAndRightTrim(20);
        Assertions.assertEquals(testString, readPadString);
        int readInt = readBuffer.readInt();
        Assertions.assertEquals(255, readInt);
    }

    @Test
    public void testPadString_Error() {

        Buffer buffer1_1 = new FixedBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_1.putPadString(StringUtils.repeat("a", 11), 10);
        });


        Buffer buffer1_2 = new FixedBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_2.putPadString(StringUtils.repeat("a", 20), 10);
        });

        Buffer buffer2 = new FixedBuffer(32);
        buffer2.putPadString(StringUtils.repeat("a", 10), 10);
    }

    @Test
    public void testPut2PrefixedBytes() {
        String test = "test";
        int endExpected = 3333;

        checkPut2PrefixedBytes(test, endExpected);
        checkPut2PrefixedBytes(null, endExpected);
        checkPut2PrefixedBytes("", endExpected);

        byte[] bytes = new byte[Short.MAX_VALUE];
        checkPut2PrefixedBytes(BytesUtils.toString(bytes), endExpected, Short.MAX_VALUE * 2);

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            byte[] bytes2 = new byte[Short.MAX_VALUE + 1];
            checkPut2PrefixedBytes(BytesUtils.toString(bytes2), endExpected, Short.MAX_VALUE * 2);
        });

    }

    private void checkPut2PrefixedBytes(String test, int expected) {
        checkPut2PrefixedBytes(test, expected, 1024);
    }

    private void checkPut2PrefixedBytes(String test, int expected, int bufferSize) {
        Buffer buffer = new FixedBuffer(bufferSize);
        if (test != null) {
            buffer.put2PrefixedBytes(test.getBytes(UTF8_CHARSET));
        } else {
            buffer.put2PrefixedBytes(null);
        }

        buffer.putInt(expected);
        byte[] buffer1 = buffer.getBuffer();

        Buffer actual = new FixedBuffer(buffer1);
        String s = actual.read2PrefixedString();
        Assertions.assertEquals(test, s);

        int i = actual.readInt();
        Assertions.assertEquals(expected, i);
    }

    @Test
    public void testPut4PrefixedBytes() {
        String test = "test";
        int endExpected = 3333;

        checkPut4PrefixedBytes(test, endExpected);
        checkPut4PrefixedBytes(null, endExpected);
        checkPut4PrefixedBytes("", endExpected);

    }

    private void checkPut4PrefixedBytes(String test, int expected) {
        Buffer buffer = new FixedBuffer(1024);
        if (test != null) {
            buffer.put4PrefixedBytes(test.getBytes(UTF8_CHARSET));
        } else {
            buffer.put4PrefixedBytes(null);
        }

        buffer.putInt(expected);
        byte[] buffer1 = buffer.getBuffer();

        Buffer actual = new FixedBuffer(buffer1);
        String s = actual.read4PrefixedString();
        Assertions.assertEquals(test, s);

        int i = actual.readInt();
        Assertions.assertEquals(expected, i);
    }

    @Test
    public void testReadByte() {

    }

    @Test
    public void testReadBoolean() {

    }

    @Test
    public void testReadInt() {

    }

    @Test
    public void testReadLong() {

    }


    @Test
    public void testReadPrefixedString() {

    }


    private byte[] intToByteArray(int intValue) {
        byte[] buffer = new byte[4];
        ByteArrayUtils.writeInt(intValue, buffer, 0);
        return buffer;
    }

    @Test
    public void testRead4PrefixedString() {
        String value = "test";

        byte[] length = intToByteArray(value.length());
        byte[] string = value.getBytes();
        byte[] result = BytesUtils.concat(length, string);


        Buffer buffer = new FixedBuffer(result);
        String prefixedString = buffer.read4PrefixedString();
        Assertions.assertEquals(prefixedString, value);

    }


    @Test
    public void testRead4PrefixedString_Null() {
        byte[] length = intToByteArray(-1);


        Buffer buffer = new FixedBuffer(length);
        String prefixedString = buffer.read4PrefixedString();
        Assertions.assertNull(prefixedString);

    }

    @Test
    public void testPut() {
        checkUnsignedByte(255);

        checkUnsignedByte(0);
    }

    /**
     * bound 1->0
     * bound 2->128
     * bound 3->16384
     * bound 4->2097152
     * bound 5->268435456
     */
    @Test
    public void testPutVInt() {
        checkVInt(Integer.MAX_VALUE, 5);
        checkVInt(25, 1);
        checkVInt(100, 1);

        checkVInt(Integer.MIN_VALUE, -1);

        checkVInt(Integer.MAX_VALUE / 2, -1);
        checkVInt(Integer.MAX_VALUE / 10, -1);
        checkVInt(Integer.MAX_VALUE / 10000, -1);

        checkVInt(Integer.MIN_VALUE / 2, -1);
        checkVInt(Integer.MIN_VALUE / 10, -1);
        checkVInt(Integer.MIN_VALUE / 10000, -1);


        checkVInt(0, -1);
        checkVInt(127, -1);
        checkVInt(128, -1);
        checkVInt(16383, -1);
        checkVInt(16384, -1);
        checkVInt(268435455, -1);
        checkVInt(268435456, -1);

    }

    private void checkVInt(int v, int offset) {
        checkVInt_bufferSize(v, offset, 32);
        if (v >= 0) {
            final int bufferSize = BytesUtils.computeVar32Size(v);
            checkVInt_bufferSize(v, offset, bufferSize);
        } else {
            final int bufferSize = BytesUtils.computeVar64Size(v);
            checkVInt_bufferSize(v, offset, bufferSize);
        }
    }

    private void checkVInt_bufferSize(int v, int offset, int bufferSize) {
        final Buffer buffer = new FixedBuffer(bufferSize);
        buffer.putVInt(v);
        if (offset != -1) {
            Assertions.assertEquals(buffer.getOffset(), offset);
        } else {
            logger.debug("{} offsetSize:{}", v, buffer.getOffset());
        }
        buffer.setOffset(0);
        int readV = buffer.readVInt();
        Assertions.assertEquals(readV, v);
    }

    @Test
    public void testPutSVInt() {
        // 63 is the boundary for a 1 byte number
        checkSVInt(63, -1);
        // 8191 is the boundary for a 2 byte number
        checkSVInt((1024 * 8) - 1, -1);

        checkSVInt(3, -1);

        checkSVInt(Integer.MAX_VALUE, 5);

        checkSVInt(Integer.MIN_VALUE, 5);

        checkSVInt(0, -1);
        checkSVInt(Integer.MAX_VALUE / 2, -1);
        checkSVInt(Integer.MAX_VALUE / 10, -1);
        checkSVInt(Integer.MAX_VALUE / 10000, -1);

        checkSVInt(Integer.MIN_VALUE / 2, -1);
        checkSVInt(Integer.MIN_VALUE / 10, -1);
        checkSVInt(Integer.MIN_VALUE / 10000, -1);


    }

    //    @Test
    public void find_SVInt_errorCode() {
        byte[] bytes = new byte[10];

        while (true) {
            random.nextBytes(bytes);
            Buffer buffer = new FixedBuffer(bytes);
            try {
                int i = buffer.readVInt();
            } catch (IllegalArgumentException e) {
                logger.debug(e.getMessage(), e);
                String binaryString = BytesUtils.toString(bytes);
                logger.debug(binaryString);
                for (byte aByte : bytes) {
                    String code = String.valueOf((int) aByte);
                    logger.debug(code);
                }
                return;
            }
        }
    }

    //    @Test
    public void find_SVLong_errorCode() {
        byte[] bytes = new byte[10];

        while (true) {
            random.nextBytes(bytes);
            Buffer buffer = new FixedBuffer(bytes);
            try {
                long i = buffer.readVLong();
            } catch (IllegalArgumentException e) {
                logger.debug(e.getMessage(), e);
                String binaryString = BytesUtils.toString(bytes);
                logger.debug(binaryString);
                for (byte aByte : bytes) {
                    String code = String.valueOf((int) aByte);
                    logger.debug(code);
                }
                return;
            }
        }
    }

    @Test
    public void readVInt_errorCase() {
        byte[] errorCode = new byte[]{-118, -41, -17, -117, -81, -115, -64, -64, -108, -88};
        Buffer buffer = new FixedBuffer(errorCode);
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            buffer.readVInt();
        });

        Assertions.assertEquals(0, buffer.getOffset());
    }

    @Test
    public void readVLong_errorCase() {
        byte[] errorCode = new byte[]{-25, -45, -47, -14, -16, -104, -53, -48, -72, -9};
        Buffer buffer = new FixedBuffer(errorCode);
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            buffer.readVLong();
        });

        Assertions.assertEquals(0, buffer.getOffset());
    }

    private void checkSVInt(int v, int offset) {
        Buffer buffer = new FixedBuffer(32);
        buffer.putSVInt(v);
        if (offset != -1) {
            Assertions.assertEquals(buffer.getOffset(), offset);
        } else {
            logger.debug("{} offsetSize:{}", v, buffer.getOffset());
        }
        buffer.setOffset(0);
        int readV = buffer.readSVInt();
        Assertions.assertEquals(readV, v);
    }

    @Test
    public void testPutVLong() {
        checkVLong(1);
        checkVLong(-1);

        checkVLong(Long.MAX_VALUE);
        checkVLong(Long.MIN_VALUE);

        checkVLong(Long.MAX_VALUE / 2);
        checkVLong(Long.MIN_VALUE / 2);

        checkVLong(Long.MAX_VALUE / 128);

        checkVLong(Long.MAX_VALUE / 102400);

        checkVLong(900719925474L);
        checkVLong(9007199254L);
        checkVLong(225179981);
        checkVLong(1179981);
        checkVLong(9981);
        checkVLong(127);
        checkVLong(-127);

        checkVLong(0L);
        checkVLong(127L);
        checkVLong(128L);
        checkVLong(16383L);
        checkVLong(16384L);
        checkVLong(268435455L);
        checkVLong(268435456L);
        checkVLong(34359738367L);
        checkVLong(34359738368L);
    }

    private void checkVLong(long v) {
        checkVLong_bufferSize(v, 32);
        final int bufferSize = BytesUtils.computeVar64Size(v);
        checkVLong_bufferSize(v, bufferSize);
    }

    private void checkVLong_bufferSize(long v, int bufferSize) {
        final Buffer buffer = new FixedBuffer(bufferSize);
        buffer.putVLong(v);

        buffer.setOffset(0);
        long readV = buffer.readVLong();
        Assertions.assertEquals(readV, v);

        if (logger.isTraceEnabled()) {
            logger.trace("v:{} offset:{}", v, buffer.getOffset());
        }
    }

    private void checkUnsignedByte(int value) {
        Buffer buffer = new FixedBuffer(1024);
        buffer.putByte((byte) value);
        byte[] buffer1 = buffer.getBuffer();

        Buffer reader = new FixedBuffer(buffer1);
        int i = reader.readUnsignedByte();
        Assertions.assertEquals(value, i);
    }


    @Test
    public void testGetBuffer() {
        Buffer buffer = new FixedBuffer(4);
        buffer.putInt(1);
        Assertions.assertEquals(buffer.getOffset(), 4);
        assertThat(buffer.getBuffer()).hasSize(4);
    }

    @Test
    public void testWrapByteBuffer() {
        FixedBuffer buffer = new FixedBuffer(8);
        buffer.putInt(1);
        buffer.putInt(2);

        final ByteBuffer byteBuffer = buffer.wrapByteBuffer();
        Assertions.assertEquals(byteBuffer.getInt(), 1);
        Assertions.assertEquals(byteBuffer.getInt(), 2);
    }

    @Test
    public void testSliceGetBuffer() {
        Buffer buffer = new FixedBuffer(5);
        buffer.putInt(1);
        Assertions.assertEquals(buffer.getOffset(), 4);
        assertThat(buffer.getBuffer()).hasSize(4);

        byte[] buffer1 = buffer.getBuffer();
        byte[] buffer2 = buffer.getBuffer();
        Assertions.assertNotSame(buffer1, buffer2);

    }

    @Test
    public void testBoolean() {
        Buffer buffer = new FixedBuffer(16);
        buffer.putBoolean(true);
        buffer.putBoolean(false);

        Buffer read = new FixedBuffer(buffer.getBuffer());
        boolean b = read.readBoolean();
        Assertions.assertTrue(b);

        boolean c = read.readBoolean();
        Assertions.assertFalse(c);
    }

    @Test
    public void setByte() {
        Buffer buffer = new FixedBuffer(16);
        buffer.putByte((byte) 5);
        buffer.putInt(123);
        buffer.putLong(456);

        buffer.setByte(0, (byte) buffer.getOffset());
        buffer.setOffset(0);

        Assertions.assertEquals(13, buffer.readByte());
        Assertions.assertEquals(123, buffer.readInt());
        Assertions.assertEquals(456, buffer.readLong());
    }

    @Test
    public void setByte1() {
        Buffer buffer = new FixedBuffer(16);
        buffer.putByte((byte) 5);
        buffer.putInt(123);
        int fixedSizeOffset = buffer.getOffset();
        buffer.putPrefixedString("abc");

        buffer.setByte(0, (byte) fixedSizeOffset);

        buffer.setOffset(fixedSizeOffset);

        Assertions.assertEquals("abc", buffer.readPrefixedString());
    }

    @Test
    public void testGetOffset() {
        Buffer buffer = new FixedBuffer();
        Assertions.assertEquals(buffer.getOffset(), 0);

        buffer.putInt(4);
        Assertions.assertEquals(buffer.getOffset(), 4);

    }


    @Test
    public void test_remaining() {
        final byte[] bytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        Buffer buffer = new FixedBuffer(bytes);
        Assertions.assertEquals(buffer.remaining(), 4);
        Assertions.assertTrue(buffer.hasRemaining());

        buffer.putInt(1234);
        Assertions.assertEquals(buffer.remaining(), 0);
        Assertions.assertFalse(buffer.hasRemaining());

        buffer.setOffset(0);
        buffer.putShort((short) 12);
        Assertions.assertEquals(buffer.remaining(), 2);
        Assertions.assertTrue(buffer.hasRemaining());

        buffer.putByte((byte) 1);
        Assertions.assertEquals(buffer.remaining(), 1);
        Assertions.assertTrue(buffer.hasRemaining());
    }


}
