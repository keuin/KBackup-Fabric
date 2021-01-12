package com.keuin.kbackupfabric.util.backup.incremental;

import com.keuin.kbackupfabric.util.backup.incremental.identifier.FileIdentifierFactory;
import com.keuin.kbackupfabric.util.backup.incremental.identifier.ObjectIdentifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Incremental backup is implemented as git-like file collection.
 * Files are called `objects`, the collection contains all files distinguished by their
 * identifiers. Usually, identifier is the combination of hash and other short information (such as size and another hash).
 * The identifier should use hashes that are strong enough, to prevent possible collisions.
 */
public class ObjectCollectionFactory <T extends ObjectIdentifier> {
    private final FileIdentifierFactory<T> identifierFactory;

    public ObjectCollectionFactory(FileIdentifierFactory<T> identifierFactory) {
        this.identifierFactory = identifierFactory;
    }

    public ObjectCollection fromDirectory(File directory) throws IOException {
        final Set<ObjectIdentifier> subFiles = new HashSet<>();
        final Set<ObjectCollection> subCollections = new HashSet<>();

        if (!Objects.requireNonNull(directory).isDirectory())
            throw new IllegalArgumentException("given file is not a directory");

        for (Iterator<Path> iter = Files.walk(directory.toPath()).iterator(); iter.hasNext();) {
            Path path = iter.next();
            File file = path.toFile();
            if (file.isDirectory()) {
                subCollections.add(fromDirectory(file));
            } else {
                subFiles.add(identifierFactory.fromFile(file));
            }
        }

        return new ObjectCollection(directory.getName(), subFiles, subCollections);
    }

}
