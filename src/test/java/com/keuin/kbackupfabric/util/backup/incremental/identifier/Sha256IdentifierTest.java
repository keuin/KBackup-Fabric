package com.keuin.kbackupfabric.util.backup.incremental.identifier;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Sha256IdentifierTest {

    @Test
    public void fromFile() {
        try {
            Sha256Identifier sha256 = Sha256Identifier.fromFile(new File("./src/test/sha256"));
            String str = sha256.getIdentification().toUpperCase();
            assertEquals("315F5BDB76D078C43B8AC0064E4A0164612B1FCE77C869345BFC94C75894EDD3", str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}