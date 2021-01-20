package com.keuin.kbackupfabric.util.backup.incremental;

import com.keuin.kbackupfabric.util.backup.incremental.identifier.Sha256Identifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class ObjectCollectionSerializerTest {
    @Test
    public void testSerializationConsistency() throws IOException {
        ObjectCollectionFactory<Sha256Identifier> factory =
                new ObjectCollectionFactory<>(Sha256Identifier.getFactory());
        ObjectCollection collection =
                factory.fromDirectory(new File("./testfile/ObjectCollectionFactoryTest"));
        File file = new File("./testfile/serialized");
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        ObjectCollectionSerializer.toFile(collection, file);
        ObjectCollection collection2 = ObjectCollectionSerializer.fromFile(file);
        Files.delete(file.toPath());
        assertEquals(collection, collection2);
    }

}