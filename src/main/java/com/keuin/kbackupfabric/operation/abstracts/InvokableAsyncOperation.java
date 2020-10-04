package com.keuin.kbackupfabric.operation.abstracts;

public abstract class InvokableAsyncOperation extends InvokableOperation {

    private final AbstractAsyncOperation asyncOperation;

    public InvokableAsyncOperation(String name) {
        asyncOperation = new AbstractAsyncOperation(name) {
            @Override
            protected void async() {
                InvokableAsyncOperation.this.async();
            }

            @Override
            protected boolean sync() {
                return InvokableAsyncOperation.this.sync();
            }
        };
    }

    protected abstract void async();

    protected boolean sync() {
        return true;
    }

    @Override
    protected boolean operate() {
        return asyncOperation.operate();
    }
}
