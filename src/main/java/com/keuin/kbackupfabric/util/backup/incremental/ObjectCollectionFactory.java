package com.keuin.kbackupfabric.util.backup.incremental;

import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.backup.incremental.identifier.FileIdentifierProvider;
import com.keuin.kbackupfabric.util.backup.incremental.identifier.ObjectIdentifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Incremental backup is implemented as git-like file collection.
 * Files are called `objects`, the collection contains all files distinguished by their
 * identifiers. Usually, identifier is the combination of hash and other short information (such as size and another hash).
 * The identifier should use hashes that are strong enough, to prevent possible collisions.
 */
public class ObjectCollectionFactory<T extends ObjectIdentifier> {

    private final FileIdentifierProvider<T> identifierFactory;

    public ObjectCollectionFactory(FileIdentifierProvider<T> identifierFactory) {
        this.identifierFactory = identifierFactory;
    }

    public ObjectCollection fromDirectory(File directory, Set<String> ignoredFiles) throws IOException {
        final Set<ObjectElement> subFiles = new HashSet<>();
        final Map<String, ObjectCollection> subCollections = new HashMap<>();

        if (!Objects.requireNonNull(directory).isDirectory())
            throw new IllegalArgumentException("given file is not a directory");

        for (Iterator<Path> iter = Files.walk(directory.toPath(), 1).iterator(); iter.hasNext(); ) {
            Path path = iter.next();
            if (Files.isSameFile(path, directory.toPath()))
                continue;
            File file = path.toFile();
            if (file.isDirectory()) {
                subCollections.put(file.getName(), fromDirectory(file, ignoredFiles));
            } else if (!ignoredFiles.contains(file.getName())) {
                subFiles.add(new ObjectElement(file.getName(), identifierFactory.fromFile(file)));
            } else {
                PrintUtil.info(String.format("Skipping file %s.", file.getName()));
            }
        }

        return new ObjectCollection(directory.getName(), subFiles, subCollections);
    }

    public ObjectCollection fromDirectory(File directory) throws IOException {
        return fromDirectory(directory, Collections.emptySet());
    }

}
