package com.keuin.kbackupfabric.util.cow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileEagerCopier implements FileCopier {
    @Override
    public void copy(String dst, String src) throws IOException {
        Files.copy(Paths.get(src), Paths.get(dst));
    }

    @Override
    public boolean isCow() {
        return false;
    }
}
