package com.keuin.kbackupfabric.backup.incremental.serializer;

import com.keuin.kbackupfabric.TestUtils;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionFactory;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionSerializer;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import com.keuin.kbackupfabric.backup.name.IncrementalBackupFileNameEncoder;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class IncBakupBackwardCompatibilityTest {

    private final String customName = "test_backup";
    private final LocalDateTime backupTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1, 1);

    private Path testRoot;
    private File indexFile;

    @Before
    public void setUp() throws IOException {
        testRoot = Paths.get(TestUtils.getTempDirectory("IncBackupBackwardCompatibilityTest"));
        indexFile = new File(testRoot.toString(), IncrementalBackupFileNameEncoder.INSTANCE.encode(customName, backupTime));
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(testRoot.toString()));
    }

    @Test
    public void testBackwardCompatibility() throws IOException {

        if (!testRoot.toFile().isDirectory()) {
            if (!testRoot.toFile().mkdirs())
                fail("Cannot initialize test environment: cannot create path.");
        }

        // now we make an old-style backup index file
        ObjectCollectionFactory<Sha256Identifier> factory =
                new ObjectCollectionFactory<>(Sha256Identifier.getFactory(), 1, 0);
        ObjectCollection2 collection = factory.fromDirectory(testRoot.toFile());
        ObjectCollectionSerializer.toFile(collection, indexFile);
        SavedIncrementalBackup info = IncBackupInfoSerializer.fromFile(indexFile);
        assertEquals(collection, info.getObjectCollection());
        assertEquals(customName, info.getBackupName());
        assertTrue(backupTime.toEpochSecond(ZoneOffset.UTC) - info.getBackupTime().toLocalDateTime().toEpochSecond(ZoneOffset.UTC) <= 2);
    }
}
