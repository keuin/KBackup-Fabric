package com.keuin.kbackupfabric.backup.incremental.identifier;

import java.io.Serializable;

/**
 * The identifier distinguishing files in the object collection.
 * It should be based on cryptographic hash function in order to prevent possible attacks to the backup system.
 * All identifiers should be immutable and implement their own equals method.
 * Immutable.
 */
public interface ObjectIdentifier extends Serializable {
    String getIdentification();
}
