package com.keuin.kbackupfabric.operation.abstracts;

public abstract class AbstractConfirmableOperation extends AbstractSerialOperation {

    public final boolean confirm() {
        return operate();
    }

    @Override
    public abstract String toString();
}
