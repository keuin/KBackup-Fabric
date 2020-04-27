package com.keuin.kbackupfabric.operation.abstracts;

public abstract class AbstractAsyncOperation extends AbstractSerializedOperation {

    private final Thread thread;
    private final String name;
    private final Object sync = new Object();

    protected AbstractAsyncOperation(String name) {
        this.name = name;
        this.thread = new Thread(this::async, name);
    }

    /**
     * Start the worker thread.
     *
     * @return true if succeed starting, false if already started.
     */
    @Override
    protected final boolean operate() {
        synchronized (sync) {
            if (thread.isAlive())
                return false;
            if (!sync())
                return false;
            thread.start();
            return true;
        }
    }

    /**
     * Implement your async operation here.
     * When this method returns, the operation must finish.
     */
    protected abstract void async();

    /**
     * If necessary, implement your sync operations here.
     * It will be invoked before starting the async thread.
     */
    protected boolean sync() {
        return true;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "operation " + name;
    }
}
