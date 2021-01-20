package com.keuin.kbackupfabric.backup.incremental.identifier;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Sha256IdentifierTest {

    @Test
    public void fromFile() {
        try {
            Sha256Identifier sha256 = Sha256Identifier.fromFile(new File("./testfile/Sha256IdentifierTest"));
            String str = sha256.getIdentification().toUpperCase();
            assertEquals("S2-315F5BDB76D078C43B8AC0064E4A0164612B1FCE77C869345BFC94C75894EDD3", str);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}