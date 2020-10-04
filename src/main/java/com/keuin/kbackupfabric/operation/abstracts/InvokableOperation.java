package com.keuin.kbackupfabric.operation.abstracts;

import com.keuin.kbackupfabric.operation.abstracts.i.Invokable;

public abstract class InvokableOperation extends AbstractSerialOperation implements Invokable {
    public boolean invoke() {
        return operate();
    }
}
