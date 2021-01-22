package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.FileIdentifierProvider;
import com.keuin.kbackupfabric.backup.incremental.identifier.ObjectIdentifier;
import com.keuin.kbackupfabric.util.PrintUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public ObjectCollection fromDirectory(File directory, Set<String> ignoredFiles) throws IOException {

        final Map<String, ObjectCollection> subCollections = new HashMap<>();

        if (!Objects.requireNonNull(directory).isDirectory())
            throw new IllegalArgumentException("given file is not a directory");


        // TODO: use putter instead
        Set<File> files = new HashSet<>();
        for (Iterator<Path> iter = Files.walk(directory.toPath(), 1).iterator(); iter.hasNext(); ) {
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

        return new ObjectCollection(directory.getName(), subFiles, subCollections);
    }

    public ObjectCollection fromDirectory(File directory) throws IOException {
        return fromDirectory(directory, Collections.emptySet());
    }

    private synchronized void fail(IOException e) {
        this.exception = e;
    }

    /**
     * A single-consumer, multiple-producer model.
     *
     * @param <Res> type of the resource to be produced and consumed.
     */
    private static class ParallelSupplier<Res> {

        private final Consumer<Res> consumer;
        private final ConcurrentLinkedQueue<Supplier<Res>> taskList = new ConcurrentLinkedQueue<>();
        private final int threads;
        private final Set<ParallelWorker<Res>> workers = new HashSet<>();

        public ParallelSupplier(Consumer<Res> consumer, int threads) {
            this.consumer = consumer;
            this.threads = threads;
        }

        public void addTask(Supplier<Res> valueSupplier) {
            this.taskList.add(valueSupplier);
        }

        public void process() {
            workers.clear();
            for (int i = 0; i < threads; i++) {
                ParallelWorker<Res> worker = new ParallelWorker<Res>(taskList, consumer, i);
                workers.add(worker);
                worker.start();
            }
            join(); // wait for all workers to exit before returning
        }

        private void join() {
            while (true) {
                int aliveCount = 0;
                for (ParallelWorker<Res> worker : workers) {
                    try {
                        if (worker.isAlive()) {
                            ++aliveCount;
                            worker.join();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                if (aliveCount == 0)
                    return;
            }
        }

        private static class ParallelWorker<V> extends Thread {

            private final Queue<Supplier<V>> taskProvider;
            private final Consumer<V> consumer;

            public ParallelWorker(Queue<Supplier<V>> taskProvider, Consumer<V> consumer, int workerId) {
                super("PutterWorker#" + workerId);
                this.taskProvider = taskProvider;
                this.consumer = consumer;
            }

            @Override
            public void run() {
                Supplier<V> supplier;
                while ((supplier = taskProvider.poll()) != null) {
                    // here we do not let the consumer accept null productions
                    Optional.ofNullable(supplier.get()).ifPresent(consumer);
                }
            }
        }
    }

//    private interface Puttable<K, V> {
//        void put(K key, V value);
//    }

}
