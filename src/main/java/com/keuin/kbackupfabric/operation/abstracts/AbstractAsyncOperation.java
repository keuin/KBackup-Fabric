package com.keuin.kbackupfabric.operation.abstracts;

/**
 * A basic async operation, but not invokable.
 * If you want an invokable interface (InvokableOperation), use InvokableAsyncOperation instead.
 */
public abstract class AbstractAsyncOperation extends AbstractSerialOperation {

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
     * @return true if succeed starting, false if this operation is already started, or the sync method failed.
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
     * After starting the operation, this method will be run in another thread after the sync method returns.
     * When this method returns, the operation must have been finished.
     */
    protected abstract void async();

    /**
     * If necessary, implement your sync operations here.
     * It will be invoked before starting the async thread.
     * If this method failed, the async method will not be invoked.
     * @return whether this method succeed.
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
