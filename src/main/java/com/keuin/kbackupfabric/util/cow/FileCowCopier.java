package com.keuin.kbackupfabric.util.cow;

import com.keuin.kbackupfabric.util.PrintUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FileCowCopier {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    static {
        try {
            System.loadLibrary("kbackup_cow");
        } catch (SecurityException | UnsatisfiedLinkError ignored) {
        }
    }

    public static native void init();

    public static native void copy(String dst, String src) throws IOException;

    public static native String getVersion();

    public static FileCopier getInstance() {
        if (initialized.compareAndSet(false, true)) {
            FileCowCopier.init();
            PrintUtil.info("kbackup-cow version: " + FileCowCopier.getVersion());
        }
        return new FileCopier() {
            @Override
            public void copy(String dst, String src) throws IOException {
                FileCowCopier.copy(dst, src);
            }

            @Override
            public boolean isCow() {
                return true;
            }
        };
    }
}
