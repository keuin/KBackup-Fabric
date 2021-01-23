package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convert legacy `ObjectCollection` (keep for backward-compatibility after refactoring the code)
 * to new `ObjectCollection2`.
 */
public class ObjectCollectionConverter {
    /**
     * Convert legacy `ObjectCollection` (keep for backward-compatibility after refactoring the code)
     * to new `ObjectCollection2`.
     *
     * @param objectCollection old instance.
     * @return new instance.
     */
    public static ObjectCollection2 convert(ObjectCollection objectCollection) {
        Map<String, ObjectCollection> oldSubCollectionMap = objectCollection.getSubCollectionMap();
        Map<String, ObjectCollection2> convertedSubCollectionMap = new HashMap<>(oldSubCollectionMap.size());
        oldSubCollectionMap.forEach((s, c) -> convertedSubCollectionMap.put(s, convert(c)));
        Set<ObjectElement> convertedElementSet = new HashSet<>();
        objectCollection.getElementSet().forEach(oldElement -> convertedElementSet.add(ObjectElementConverter.convert(oldElement)));
        return new ObjectCollection2(objectCollection.getName(), convertedElementSet, convertedSubCollectionMap);
    }
}
