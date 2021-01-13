package com.keuin.kbackupfabric.util.backup.name;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

public class PrimitiveBackupFileNameEncoderTest {

    @Test
    public void testConsistency() {
        LocalDateTime time = LocalDateTime.ofEpochSecond(System.currentTimeMillis()/1000, 0, ZoneOffset.UTC);
        String name = "Test Na_me";
        PrimitiveBackupFileNameEncoder encoder = new PrimitiveBackupFileNameEncoder();
        BackupFileNameEncoder.BackupBasicInformation information = encoder.decode(encoder.encode(name, time));
        assertEquals(time, information.time);
        assertEquals(name, information.customName);
    }
}