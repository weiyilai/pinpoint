/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.wd;


import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.ThreadLocalRandom;


/**
 *
 */
public class RangeDoubleHash implements ByteHasher {
    private final int start;
    private final int end;
    private final int maxBuckets;

    private final int secondaryMod;

    private final byte[][] prefix;

    public RangeDoubleHash(int start, int end, int maxBuckets, int secondaryMod) {
        if (maxBuckets < 1 || maxBuckets > MAX_BUCKETS) {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }

        this.start = start;
        this.end = end;
        // i.e. "real" maxBuckets value = maxBuckets or maxBuckets-1
        this.maxBuckets = maxBuckets;

        this.secondaryMod = secondaryMod;

        this.prefix = toModBytes(this.maxBuckets, secondaryMod);
    }

    private byte[][] toModBytes(int mod, int secondaryMod) {
        byte[][] prefixBytes = new byte[mod][secondaryMod];
        for (int i = 0; i < mod; i++) {
            byte[] modBytes = new byte[secondaryMod];
            for (int j = 0; j < secondaryMod; j++) {
                modBytes[j] = (byte) secondaryModIndex(i, j);
            }
            prefixBytes[i] = modBytes;
        }
        return prefixBytes;
    }


    @Override
    public byte getHashPrefix(byte[] originalKey) {
        int index = firstIndex(originalKey);

        int secondaryIndex = secondaryIndex(originalKey);
        index = secondaryModIndex(index, secondaryIndex);

        return (byte) index;
    }

    int secondaryModIndex(int index, int secondaryIndex) {
        return (index + secondaryIndex) % maxBuckets;
    }

    int firstIndex(byte[] originalKey) {
        return MathUtils.fastAbs(hashBytes(originalKey)) % maxBuckets;
    }

    protected int secondaryIndex(byte[] originalKey) {
        // Random distribution
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextInt(secondaryMod);
    }

    /** Compute hash for binary data. */
    private int hashBytes(byte[] bytes) {
        int length = Math.min(bytes.length, end);
        return BytesUtils.hashBytes(bytes, start, length);
    }

    @Override
    public byte[] getAllPossiblePrefixes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getAllPossiblePrefixes(byte[] originalKey) {
        int index = firstIndex(originalKey);
        return this.prefix[index];
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return 1;
    }

}
