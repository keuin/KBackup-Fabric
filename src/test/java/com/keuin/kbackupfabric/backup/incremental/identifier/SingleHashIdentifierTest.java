package com.keuin.kbackupfabric.backup.incremental.identifier;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SingleHashIdentifierTest {

    @Test
    public void testEquals() {
        SingleHashIdentifier id1 = new ConcreteSingleHashIdentifier(new byte[]{1, 2, 3, 4}, "ahash");
        SingleHashIdentifier id2 = new ConcreteSingleHashIdentifier(new byte[]{1, 2, 3, 4}, "ahash");
        SingleHashIdentifier id3 = new ConcreteSingleHashIdentifier(new byte[]{1, 2, 3}, "ahash");
        SingleHashIdentifier id4 = new ConcreteSingleHashIdentifier(new byte[]{1, 2, 3, 4}, "a");
        assertEquals(id1, id1);
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id1, id4);
        assertNotEquals(id3, id4);
    }

    private static class ConcreteSingleHashIdentifier extends SingleHashIdentifier {
        protected ConcreteSingleHashIdentifier(byte[] hash, String type) {
            super(hash, type);
        }

        @Override
        protected byte[] hash(File file) throws IOException {
            return new byte[0];
        }
    }
}