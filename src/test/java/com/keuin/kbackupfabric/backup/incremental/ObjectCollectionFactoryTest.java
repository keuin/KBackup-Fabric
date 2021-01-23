package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectCollectionFactoryTest {

    private void validate(ObjectCollection2 collection, List<String> subCollections, Map<String, String> subElements) {
        assertEquals(subCollections.size(), collection.getSubCollectionMap().size());
        assertEquals(subElements.size(), collection.getElementSet().size());
        for (Map.Entry<String, ObjectCollection2> c : collection.getSubCollectionMap().entrySet()) {
            assertEquals(c.getKey(), c.getValue().getName());
            assertTrue(subCollections.contains(c.getKey()));
        }
        for (Map.Entry<String, ObjectElement> entry : collection.getElementMap().entrySet()) {
//            assertTrue(subElements.contains(e.getIdentification()));
            assertEquals(subElements.get(entry.getKey()).toUpperCase(), entry.getValue().getIdentifier().getIdentification().toUpperCase());
        }
    }

    @Test
    public void fromDirectory1() {
        fromDirectory(1, 0);
    }

    @Test
    public void fromDirectory2() {
        fromDirectory(2, 0);
    }

    @Test
    public void fromDirectory4() {
        fromDirectory(4, 0);
    }

    @Test
    public void fromDirectory8() {
        fromDirectory(8, 0);
    }

    @Test
    public void fromDirectory1A() {
        fromDirectory(1, 1000);
    }

    @Test
    public void fromDirectory2A() {
        fromDirectory(2, 1000);
    }

    @Test
    public void fromDirectory4A() {
        fromDirectory(4, 1000);
    }

    @Test
    public void fromDirectory8A() {
        fromDirectory(8, 1000);
    }

    public void fromDirectory(int threads, int multiThreadThreshold) {
        try {
            ObjectCollectionFactory<Sha256Identifier> factory =
                    new ObjectCollectionFactory<>(Sha256Identifier.getFactory(), threads, multiThreadThreshold);
            ObjectCollection2 collection =
                    factory.fromDirectory(new File("./testfile/ObjectCollectionFactoryTest"));

            assertEquals("ObjectCollectionFactoryTest", collection.getName());
            assertEquals(3, collection.getSubCollectionMap().size());
            assertEquals(2, collection.getElementSet().size());

            final Map<String, String> elements = new HashMap<>();

            // check root dir
            elements.put("a", "S2-261CA0D59FEE8FD169802BB8030A07CF23E5C1593FA81A16C6D0A8CF27DAA2ED");
            elements.put("b", "S2-B3FED75012C4969DC63A50EBC4E745FF77E4A06E0B04720EF71EF033032EBAF7");
            validate(collection, Arrays.asList("1", "2", "3"), elements);
            elements.clear();

            // check `1`
            elements.put("a", "S2-E8620F35A5DB33B1257CC51245DDACDA8AF3E0D431A8A38473575E468BCBD0BD");
            elements.put("b", "S2-19EE41585A674274891DE5A4B365DBAB9C49C576AB6F86CD515B683724D2DBBD");
            validate(collection.getSubCollectionMap().get("1"), Arrays.asList("11", "12"), elements);
            elements.clear();

            // check `2`
            validate(collection.getSubCollectionMap().get("2"), Collections.emptyList(), Collections.emptyMap());

            // check `3`
            validate(collection.getSubCollectionMap().get("3"), Collections.emptyList(), Collections.emptyMap());

            // check `11`
            validate(collection.getSubCollectionMap().get("1").getSubCollectionMap().get("11"), Collections.singletonList("111"), Collections.emptyMap());

            // check `111`
            elements.put("a", "S2-1EDBE882A757E1FAFCA77A9D3BE3FF5D2BB3E2037B238C865F1F957C431F43B4");
            elements.put("b", "S2-30BA7CD8B4AD93A8B3826CD8D1518790924EEBB930EC04DF7DFB03A50B17D7BC");
            validate(
                    collection.getSubCollectionMap().get("1").getSubCollectionMap().get("11").getSubCollectionMap().get("111"),
                    Collections.emptyList(),
                    elements
            );
            elements.clear();

            // check `12`
            validate(collection.getSubCollectionMap().get("1").getSubCollectionMap().get("12"), Collections.emptyList(), Collections.emptyMap());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}