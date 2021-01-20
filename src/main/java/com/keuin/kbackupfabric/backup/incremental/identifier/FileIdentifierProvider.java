package com.keuin.kbackupfabric.backup.incremental.identifier;

import java.io.File;
import java.io.IOException;

public interface FileIdentifierProvider<T extends ObjectIdentifier> {
    /**
     * Generate file identifier from a random file. The file is not necessarily in the object base.
     *
     * @param file the file.
     * @return the file identifier.
     * @throws IOException when an I/O error occurs.
     */
    T fromFile(File file) throws IOException;
}
