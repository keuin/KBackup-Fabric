package com.keuin.kbackupfabric.config;

import com.keuin.kbackupfabric.TestUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class KBackupConfigTest {

    @Test
    public void load() throws Exception {
        String tempDir = TestUtils.getTempDirectory("config_test");
        Files.createDirectories(Paths.get(tempDir));
        Path configPath = Paths.get(tempDir, "test_config.json");
        Files.write(
                configPath,
                "{\n//comment\n}".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        System.out.println(configPath.toFile().getAbsolutePath());
        KBackupConfig.load(configPath.toFile());
    }
}