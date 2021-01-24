package com.keuin.kbackupfabric.backup.name;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class PrimitiveBackupFileNameEncoderTest {

    @Test
    public void testConsistency() {
        LocalDateTime time = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);
        String name = "Test Na_me";
        PrimitiveBackupFileNameEncoder encoder = PrimitiveBackupFileNameEncoder.INSTANCE;
        BackupFileNameEncoder.BackupBasicInformation information = encoder.decode(encoder.encode(name, time));
        assertEquals(time, information.time);
        assertEquals(name, information.customName);
    }

    @Test
    public void testEncode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        PrimitiveBackupFileNameEncoder encoder = PrimitiveBackupFileNameEncoder.INSTANCE;
        assertEquals("kbackup-0001-01-01_01-01-01_name.zip", encoder.encode(customName, time));
    }

    @Test
    public void testDecode() {
        LocalDateTime time = LocalDateTime.of(1, 1, 1, 1, 1, 1);
        String customName = "name";
        PrimitiveBackupFileNameEncoder encoder = PrimitiveBackupFileNameEncoder.INSTANCE;
        BackupFileNameEncoder.BackupBasicInformation information = encoder.decode("kbackup-0001-01-01_01-01-01_name.zip");
        assertEquals(time, information.time);
        assertEquals(customName, information.customName);
    }

    @Test
    public void isValid() {
        PrimitiveBackupFileNameEncoder encoder = PrimitiveBackupFileNameEncoder.INSTANCE;
        assertTrue(encoder.isValidFileName("kbackup-0001-01-01_01-01-01_name.zip"));
        assertTrue(encoder.isValidFileName("kbackup-0001-01-01_01-01-01_0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFileName("kbackup-0001-01-01_01-01-01kbackup-0001-01-01_01-01-01_name.zip"));
        assertFalse(encoder.isValidFileName("kbackup-0001-01-01_01-01-01_name"));
        assertFalse(encoder.isValidFileName("kbackup-0001-01-01_01-01-01_name.kbi"));
        assertFalse(encoder.isValidFileName("somefile"));
    }
}