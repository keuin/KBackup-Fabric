package com.keuin.kbackupfabric.operation.abstracts;

public abstract class AbstractSerializedOperation {
    /**
     * Do your operation here.
     * This method is not designed to be public.
     * When this method returns, the operation must have finished.
     *
     * @return the stat code.
     */
    protected abstract boolean operate();
}
