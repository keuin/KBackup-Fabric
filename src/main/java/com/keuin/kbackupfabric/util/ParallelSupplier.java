package com.keuin.kbackupfabric.util;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A single-consumer, multiple-producer model.
 *
 * @param <Res> type of the resource to be produced and consumed.
 */
public class ParallelSupplier<Res> {

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
            ParallelWorker<Res> worker = new ParallelWorker<>(taskList, consumer, i);
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
