package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Identifier based on sha256.
 * Immutable.
 */
public class Sha256Identifier extends SingleHashIdentifier {

    private static final int SHA256_LENGTH = 32;
    private static final Sha256Identifier DUMMY = new Sha256Identifier(new byte[SHA256_LENGTH]); // only for using its hash method
    private static final FileIdentifierFactory<Sha256Identifier> factory = Sha256Identifier::fromFile;

    public static Sha256Identifier fromFile(File file) throws IOException {
        if (!Objects.requireNonNull(file).isFile()) {
            throw new IllegalArgumentException("file is not a file");
        }
        return new Sha256Identifier(DUMMY.hash(file));
    }

    public static FileIdentifierFactory<Sha256Identifier> getFactory() {
        return factory;
    }

    protected Sha256Identifier(byte[] hash) {
        super(hash);
        Objects.requireNonNull(hash);
        if (hash.length != SHA256_LENGTH) {
            throw new IllegalStateException(String.format("SHA256 must be %d bytes", SHA256_LENGTH));
        }
    }

    @Override
    protected byte[] hash(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            FileInputStream inputStream = new FileInputStream(file);

            // This does not work. I don't know why
//            FileChannel channel = inputStream.getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate(128);
//            int readLength;
//            while ((readLength = channel.read(buffer)) > 0)
//                digest.update(buffer);

            // This also works, without warnings
            byte[] readBuffer = new byte[1024 * 1024];
            int readLength;
            while ((readLength = inputStream.read(readBuffer)) > 0)
                digest.update(readBuffer,0, readLength);

            // The below lines also works, but the IDE will complain about the while loop
//            DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
//            while(digestInputStream.read() > 0)
//                ;

            return digest.digest();
        } catch (NoSuchAlgorithmException ignored) {
            // this shouldn't happen
            return new byte[SHA256_LENGTH];
        }
    }

}
