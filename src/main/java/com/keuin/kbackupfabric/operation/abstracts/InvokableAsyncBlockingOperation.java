package com.keuin.kbackupfabric.operation.abstracts;

import com.keuin.kbackupfabric.operation.abstracts.i.Blocking;
import com.keuin.kbackupfabric.operation.abstracts.i.Invokable;

public abstract class InvokableAsyncBlockingOperation implements Invokable, Blocking {

    private final InvokableAsyncOperation asyncOperation;
    private final HackedBlockingOperation blockingOperation;

    public InvokableAsyncBlockingOperation(String name) {
        asyncOperation = new InvokableAsyncOperation(name) {
            @Override
            protected void async() {
                InvokableAsyncBlockingOperation.this.async();
                // When the async operation finishes, unblock
                blockingOperation.noUnblocking(false);
                blockingOperation.block(false);
            }

            @Override
            protected boolean sync() {
                return InvokableAsyncBlockingOperation.this.sync();
            }
        };

        blockingOperation = new HackedBlockingOperation();
    }

    @Override
    public boolean invoke() {
        return blockingOperation.invoke();
    }

    @Override
    public boolean isBlocked() {
        return blockingOperation.isBlocked();
    }

    protected abstract void async();

    protected boolean sync() {
        return true;
    }

    private class HackedBlockingOperation extends InvokableBlockingOperation {

        @Override
        protected boolean blockingContext() {

            noUnblocking(true);
            return asyncOperation.invoke();
        }

        @Override
        public void noUnblocking(boolean b) {
            super.noUnblocking(b);
        }

        @Override
        public void block(boolean blockState) {
            super.block(blockState);
        }
    }

}
