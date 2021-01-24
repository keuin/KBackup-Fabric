package com.keuin.kbackupfabric.backup.name;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class IncrementalBackupFileNameEncoderTest {
    @Test
    public void testEncode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        IncrementalBackupFileNameEncoder encoder = IncrementalBackupFileNameEncoder.INSTANCE;
        assertEquals("incremental-0001-01-01_01-01-01_name.kbi", encoder.encode(customName, time));
    }

    @Test
    public void testDecode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        IncrementalBackupFileNameEncoder encoder = IncrementalBackupFileNameEncoder.INSTANCE;
        BackupFileNameEncoder.BackupBasicInformation information = encoder.decode("incremental-0001-01-01_01-01-01_name.kbi");
        assertEquals(time, information.time);
        assertEquals(customName, information.customName);
    }

    @Test
    public void isValid() {
        IncrementalBackupFileNameEncoder encoder = IncrementalBackupFileNameEncoder.INSTANCE;
        assertTrue(encoder.isValidFileName("incremental-0001-01-01_01-01-01_name.kbi"));
        assertTrue(encoder.isValidFileName("incremental-0001-01-01_01-01-01_0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFileName("incremental-0001-01-01_01-01-01incremental-0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFileName("incremental-0001-01-01_01-01-01_name"));
        assertFalse(encoder.isValidFileName("incremental-0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFileName("somefile"));
    }
}