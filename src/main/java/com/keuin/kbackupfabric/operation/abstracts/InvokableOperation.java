package com.keuin.kbackupfabric.operation.abstracts;

import com.keuin.kbackupfabric.operation.abstracts.i.Invokable;

public abstract class InvokableOperation extends AbstractSerializedOperation implements Invokable {
    public boolean invoke() {
        return operate();
    }
}
