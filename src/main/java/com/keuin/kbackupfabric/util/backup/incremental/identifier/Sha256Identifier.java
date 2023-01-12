package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import com.keuin.kbackupfabric.util.BytesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier.sha256Hash;

/**
 * Identifier based on sha256.
 * Immutable.
 */
public class Sha256Identifier extends SingleHashIdentifier {

    private static final long serialVersionUID = 968324214777435054L;
    private static final int SHA256_LENGTH = 32;
    private static final Sha256Identifier DUMMY = new Sha256Identifier(new byte[SHA256_LENGTH]); // only for using its hash method
    private static final String marker = "S2";

    public static Sha256Identifier fromFile(File file) throws IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("file is not a file");
        }
        return new Sha256Identifier(DUMMY.hash(file));
    }

    /**
     * Load sha-256 from a named file. Only used in StorageObjectLoader.
     *
     * @param fileName the file name.
     * @return identifier.
     */
    static Sha256Identifier fromFileName(String fileName) {
        if (!fileName.matches(marker + "-[0-9A-Fa-f]{32}"))
            return null;
        String hexString = fileName.substring(marker.length() + 1);
        return new Sha256Identifier(BytesUtil.hexToBytes(hexString));
    }

    protected Sha256Identifier(byte[] hash) {
        super(hash, marker);
        Objects.requireNonNull(hash);
        if (hash.length != SHA256_LENGTH) {
            throw new IllegalStateException(String.format("SHA256 must be %d bytes", SHA256_LENGTH));
        }
    }

    @Override
    protected byte[] hash(File file) throws IOException {
        return sha256Hash(file);
    }

}