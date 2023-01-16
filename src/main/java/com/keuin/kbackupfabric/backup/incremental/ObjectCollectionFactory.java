package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.FileIdentifierProvider;
import com.keuin.kbackupfabric.backup.incremental.identifier.ObjectIdentifier;
import com.keuin.kbackupfabric.util.ParallelSupplier;
import com.keuin.kbackupfabric.util.PrintUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Incremental backup is implemented as git-like file collection.
 * Files are called `objects`, the collection contains all files distinguished by their
 * identifiers. Usually, identifier is the combination of hash and other short information (such as size and another hash).
 * The identifier should use hashes that are strong enough, to prevent possible collisions.
 */
public class ObjectCollectionFactory<T extends ObjectIdentifier> {

    private final FileIdentifierProvider<T> identifierFactory;
    private final int threads;
    private Exception exception = null; // fail in async
    private final int minParallelProcessFileCountThreshold;

    public ObjectCollectionFactory(FileIdentifierProvider<T> identifierFactory, int threads, int minParallelProcessFileCountThreshold) {
        this.identifierFactory = identifierFactory;
        this.threads = threads;
        this.minParallelProcessFileCountThreshold = minParallelProcessFileCountThreshold;
        if (threads <= 0)
            throw new IllegalArgumentException("thread count must be positive.");
        if (minParallelProcessFileCountThreshold < 0)
            throw new IllegalArgumentException("minParallelProcessFileCountThreshold must not be negative.");
    }

    public ObjectCollection2 fromDirectory(File directory, Set<String> ignoredFiles) throws IOException {

        final Map<String, ObjectCollection2> subCollections = new HashMap<>();

        if (!Objects.requireNonNull(directory).isDirectory())
            throw new IllegalArgumentException("given file is not a directory");

        Set<File> files = new HashSet<>();
        try (Stream<Path> walk = Files.walk(directory.toPath(), 1)) {
            for (Iterator<Path> iter = walk.iterator(); iter.hasNext(); ) {
                Path path = iter.next();
                if (Files.isSameFile(path, directory.toPath()))
                    continue;
                File file = path.toFile();
                if (file.isDirectory()) {
                    subCollections.put(file.getName(), fromDirectory(file, ignoredFiles));
                } else if (!ignoredFiles.contains(file.getName())) {
                    files.add(file); // add to the set to be processed
                } else {
                    PrintUtil.info(String.format("Skipping file %s.", file.getName()));
                }
            }
        }

        final Set<ObjectElement> subFiles = ConcurrentHashMap.newKeySet(files.size());

        // deal with all direct sub files
        if (threads == 1 || files.size() < minParallelProcessFileCountThreshold) {
            for (File file : files) {
                subFiles.add(new ObjectElement(file.getName(), identifierFactory.fromFile(file)));
            }
        } else {
            // use ParallelSupplier to process
            ParallelSupplier<ObjectElement> parallelSupplier = new ParallelSupplier<>(subFiles::add, threads);
            files.forEach(file -> parallelSupplier.addTask(() -> {
                try {
                    return new ObjectElement(file.getName(), identifierFactory.fromFile(file));
                } catch (IOException e) {
                    fail(e);
                }
                return null;
            }));
            parallelSupplier.process();
        }

        // check if any exception has been thrown in async workers.
        synchronized (this) {
            if (this.exception != null) {
                if (exception instanceof IOException)
                    throw (IOException) exception;
                else
                    throw new RuntimeException(exception);
            }
        }

        return new ObjectCollection2(directory.getName(), subFiles, subCollections);
    }

    public ObjectCollection2 fromDirectory(File directory) throws IOException {
        return fromDirectory(directory, Collections.emptySet());
    }

    private synchronized void fail(IOException e) {
        this.exception = e;
    }

}
