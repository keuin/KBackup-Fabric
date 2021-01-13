package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import java.io.File;
import java.io.IOException;

public interface FileIdentifierProvider<T extends ObjectIdentifier> {
    T fromFile(File file) throws IOException;
}
