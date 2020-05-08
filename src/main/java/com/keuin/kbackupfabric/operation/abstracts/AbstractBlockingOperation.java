package com.keuin.kbackupfabric.operation.abstracts;

import com.keuin.kbackupfabric.operation.abstracts.i.Blocking;

public abstract class AbstractBlockingOperation extends AbstractSerializedOperation implements Blocking {

    private static final Object sync = new Object();
    private static boolean isBlocking = false;
    private boolean wasBlocked = false;
    private boolean noUnblocking = false;

    @Override
    protected final boolean operate() {
        synchronized (sync) {
            if (isBlocking) {
//                System.out.println("blocked.");
                wasBlocked = true;
                return false;
            } else {
//                System.out.println("not blocked.");
                wasBlocked = false;
                isBlocking = true;
            }
        }
        boolean exitCode = blockingContext();
        if (!noUnblocking)
            isBlocking = false;
        return exitCode;
    }

    public final boolean isBlocked() {
        return wasBlocked;
    }

    protected final void block(boolean blockState) {
        isBlocking = blockState;
    }

    protected void noUnblocking(boolean b) {
        noUnblocking = b;
    }

    protected boolean blockAndGet() {
        synchronized (sync) {
            boolean b = isBlocking;
            isBlocking = true;
            return b;
        }
    }

    /**
     * Implement your blocked operation here.
     *
     * @return the stat code.
     */
    protected abstract boolean blockingContext();

}
