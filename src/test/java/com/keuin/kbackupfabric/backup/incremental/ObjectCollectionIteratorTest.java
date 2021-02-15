package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.FileIdentifierProvider;
import com.keuin.kbackupfabric.backup.incremental.identifier.ObjectIdentifier;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
                "S2-092FCFBBCFCA3B5BE7AE1B5E58538E92C35AB273AE13664FED0D67484C8E78A6",
                "S2-A2BBDB2DE53523B8099B37013F251546F3D65DBE7A0774FA41AF0A4176992FD4",
                "S2-A3A5E715F0CC574A73C3F9BEBB6BC24F32FFD5B67B387244C2C909DA779A1478",
                "S2-0263829989B6FD954F72BAAF2FC64BC2E2F01D692D4DE72986EA808F6E99813F",
                "S2-53C234E5E8472B6AC51C1AE1CAB3FE06FAD053BEB8EBFD8977B010655BFDD3C3",
                "S2-D8FF994D310BD1F7582DC285366C3292DEB6A8F2EC64A4E7D0B3004190200034",
                "S2-8D74BEEC1BE996322AD76813BAFB92D40839895D6DD7EE808B17CA201EAC98BE",
                "S2-4355A46B19D348DC2F57C046F8EF63D4538EBB936000F3C9EE954A27460DD865",
                "S2-87428FC522803D31065E7BCE3CF03FE475096631E5E07BBD7A0FDE60C4CF25C7",
                "S2-1121CFCCD5913F0A63FEC40A6FFD44EA64F9DC135C66634BA001D10BCF4302A2",
                "S2-AE6C381493F88DA4351218C39BE5287541C9F9D4312A941E431EB4371BC515B7",
                "S2-5E1B3B203B8D9C1FE1424420B5D56A8244880E1A7539EB7E88B035EC0257FAFE"
        ));
        assertEquals(desiredIdSet, idSet.stream().map(ObjectIdentifier::getIdentification).collect(Collectors.toSet()));
    }
}