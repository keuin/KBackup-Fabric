package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import com.keuin.kbackupfabric.util.BytesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * A simple identifier based on a single hash function.
 * Immutable.
 */
public abstract class SingleHashIdentifier implements ObjectIdentifier {
    private final byte[] hash;
    private final String type;

    protected SingleHashIdentifier(byte[] hash, String type) {
        Objects.requireNonNull(hash);
        Objects.requireNonNull(type);
        this.hash = Arrays.copyOf(hash, hash.length);
        this.type = type;
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
        return type + "-" + BytesUtil.bytesToHex(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleHashIdentifier that = (SingleHashIdentifier) o;
        return Arrays.equals(hash, that.hash) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(hash);
        return result;
    }
}
