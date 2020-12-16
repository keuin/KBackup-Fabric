package com.keuin.kbackupfabric.util.backup.inc;

/**
 * Incremental backup is implemented as git-like file collection.
 * Files are called `objects`, the collection contains all files distinguished by their
 * identifiers. Usually, identifier is the combination of hash and other short information (such as size and another hash).
 * The identifier should use hashes that are strong enough, to prevent possible collisions.
 */
public class ObjectCollectionManager {

}
