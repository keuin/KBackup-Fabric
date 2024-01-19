package com.keuin.kbackupfabric.util.cow;

import java.io.IOException;

public interface FileCopier {
    void copy(String dst, String src) throws IOException;

    boolean isCow();
}
