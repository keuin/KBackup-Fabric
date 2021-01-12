package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import com.keuin.kbackupfabric.util.BytesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * A simple identifier based on a single hash function.
 */
public abstract class SingleHashIdentifier implements ObjectIdentifier {

    private final byte[] hash;

    protected SingleHashIdentifier(byte[] hash) {
        this.hash = Arrays.copyOf(hash, hash.length);
    }

    /**
     * The hash function.
     *
     * @param file the file to be hashed.
     * @return the hash bytes.
     */
    protected abstract byte[] hash(File file) throws IOException;

    @Override
    public String getIdentification() {
        return BytesUtil.bytesToHex(hash);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SingleHashIdentifier)) {
            return false;
        }
        return Arrays.equals(hash, ((SingleHashIdentifier) obj).hash);
    }
}
