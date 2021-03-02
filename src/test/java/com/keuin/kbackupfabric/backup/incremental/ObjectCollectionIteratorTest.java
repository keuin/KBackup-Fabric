package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.ObjectIdentifier;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ObjectCollectionIteratorTest {
    @Test
    public void testObjectCollectionIterator() throws IOException {
        final String testRoot = "testfile/ObjectCollectionIteratorTest/col1";
        ObjectCollection2 col =
                new ObjectCollectionFactory<>
                        (Sha256Identifier::fromFile, 1, 10)
                        .fromDirectory(new File(testRoot));
        ObjectCollectionIterator iter = new ObjectCollectionIterator(col);
        Set<ObjectIdentifier> idSet = new HashSet<>();
        Set<String> nameSet = new HashSet<>();
        iter.forEachRemaining(ele -> {
            idSet.add(ele.getIdentifier());
            nameSet.add(ele.getName());
        });
        assertEquals(new HashSet<>(Arrays.asList("1", "2", "3", "a", "b", "c", "d", "e", "f", "z1", "z2", "z3")), nameSet);
        Set<String> desiredIdSet = new HashSet<>(Arrays.asList(
                "S2-3C417B7EA567C3115DEEBED7319DE56C4D008E6990B0D45ED5CFA53D4C5D37FA",
                "S2-44BA554E17977B31413D531E0AA4E02E87A7A38115D3BE3B33C95F85FAC2A4F7",
                "S2-2E7D2C03A9507AE265ECF5B5356885A53393A2029D241394997265A1A25AEFC6",
                "S2-252F10C83610EBCA1A059C0BAE8255EBA2F95BE4D1D7BCFA89D7248A82D9F111",
                "S2-47D1607EFC92E4E3B765BE65C7EC2AC063524455D36AE201AEC7CCCD4A6E431E",
                "S2-CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB",
                "S2-18AC3E7343F016890C510E93F935261169D9E3F565436429830FAF0934F4F8E4",
                "S2-4E07408562BEDB8B60CE05C1DECFE3AD16B72230967DE01F640B7E4729B49FCE",
                "S2-D4735E3A265E16EEE03F59718B9B5D03019C07D8B6C51F90DA3A666EEC13AB35",
                "S2-3F79BB7B435B05321651DAEFD374CDC681DC06FAA65E374E38337B88CA046DEA",
                "S2-6B86B273FF34FCE19D6B804EFF5A3F5747ADA4EAA22F1D49C01E52DDB7875B4B",
                "S2-3E23E8160039594A33894F6564E1B1348BBD7A0088D42C4ACB73EEAED59C009D"
        ));
        assertEquals(desiredIdSet, idSet.stream().map(ObjectIdentifier::getIdentification).collect(Collectors.toSet()));
    }
}