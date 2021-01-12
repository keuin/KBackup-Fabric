package com.keuin.kbackupfabric.util.backup.incremental.identifier;

/**
 * The identifier distinguishing files in the object collection.
 * It should be based on cryptographic hash function in order to prevent possible attacks to the backup system.
 * All identifiers should be immutable and implement their own equals method.
 */
public interface ObjectIdentifier {
    String getIdentification();
}
