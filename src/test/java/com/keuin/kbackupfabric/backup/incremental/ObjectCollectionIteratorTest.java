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
                "S2-F1B2F662800122BED0FF255693DF89C4487FBDCF453D3524A42D4EC20C3D9C04",
                "S2-DF4E26A04A444901B95AFEF44E4A96CFAE34690FFF2AD2C66389C70079CDFF2B",
                "S2-24BA1E99DC06B19351323AAE0D7370243D586475A634B7F6FF7927FBC72CFAED",
                "S2-8E4621379786EF42A4FEC155CD525C291DD7DB3C1FDE3478522F4F61C03FD1BD",
                "S2-679E273F78FC8F8BA114DB23C2DCE80CC77C91083939825CA830152F2F080D08",
                "S2-EF1FAC987A48A7C02176F7E1C2D0E5CBDA826C9558290BA153C90EA16D5D5A96",
                "S2-39E275DAD5FC449C721EBA61F6524ADDDE80C31FA2D698124F3BF1C9E622664D",
                "S2-18CD8A3E1EFEF1F4B92DBB65BA09F50CF7332512324BCAE2F4531BA4EAB0E60A",
                "S2-D1197131ACE5A85BC0C76192B742E92DFFA2AC763A119B705F326827B087F314",
                "S2-6F9047EB742EFC9D495C531E3CDD11C87B420B5913DEE4E2893C6CF095395D16",
                "S2-780B590E823368F04FF6782EEE4E506BD5F504460CF797C02180B4569A64CD03",
                "S2-12862F6E181922C4B652768CF8D4128626B83631B1990E9B8DF54BE2B725BC5D"
        ));
        assertEquals(desiredIdSet, idSet.stream().map(ObjectIdentifier::getIdentification).collect(Collectors.toSet()));
    }
}