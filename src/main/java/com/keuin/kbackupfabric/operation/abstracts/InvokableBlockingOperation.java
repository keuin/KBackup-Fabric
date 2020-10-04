package com.keuin.kbackupfabric.operation.abstracts;

import com.keuin.kbackupfabric.operation.abstracts.i.Blocking;

public abstract class InvokableBlockingOperation extends InvokableOperation implements Blocking {

    private final AbstractBlockingOperation operation = new AbstractBlockingOperation() {
        @Override
        protected boolean blockingContext() {
            return InvokableBlockingOperation.this.blockingContext();
        }
    };

    @Override
    protected final boolean operate() {
        return operation.operate();
    }

    /**
     * Implement your blocked operation here.
     *
     * @return stat code.
     */
    protected abstract boolean blockingContext();

    protected void block(boolean blockState) {
        operation.block(blockState);
    }

    @Deprecated
    protected void noUnblocking(boolean b) {
        operation.noUnblocking(b);
    }

    @Override
    public final boolean isBlocked() {
        return operation.isBlocked();
    }

}
