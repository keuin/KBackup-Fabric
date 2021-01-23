package com.keuin.kbackupfabric.util.backup.incremental;

import java.io.Serializable;
import java.util.*;

/**
 * This class must be in package `com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection`,
 * or it will not be compatible with old backups.
 * It remains only to keep a backward compatibility, and should be converted to `ObjectCollection2` as soon as possible.
 * Thus, this class is marked as `Depreciated`. However, it should not be removed since it is needed to read legacy
 * backups correctly. But new codes should not use this class any more.
 */
@Deprecated
public class ObjectCollection implements Serializable {

    private static final long serialVersionUID = -3098905094513096717L;
    private final String name;
    private final Map<String, ObjectElement> elements;
    private final Map<String, ObjectCollection> subCollections;

    ObjectCollection(String name, Set<ObjectElement> elements, Map<String, ObjectCollection> subCollections) {
        this.name = Objects.requireNonNull(name);
        this.elements = new HashMap<>();
        for (ObjectElement e : elements) {
            Objects.requireNonNull(e);
            if (this.elements.put(e.getName(), e) != null) {
                throw new IllegalStateException("elements conflict with the same name");
            }
        }
        this.subCollections = new HashMap<>(Objects.requireNonNull(subCollections));
    }

    public String getName() {
        return name;
    }

    public Set<ObjectElement> getElementSet() {
        return new HashSet<>(elements.values());
    }

    public Map<String, ObjectElement> getElementMap() {
        return Collections.unmodifiableMap(elements);
    }

    public ObjectElement getElement(String name) {
        return elements.get(name);
    }

    public Set<ObjectCollection> getSubCollectionSet() {
        return new HashSet<>(subCollections.values());
    }

    public Map<String, ObjectCollection> getSubCollectionMap() {
        return Collections.unmodifiableMap(subCollections);
    }

    public ObjectCollection getSubCollection(String name) {
        return subCollections.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectCollection that = (ObjectCollection) o;
        return name.equals(that.name) &&
                elements.equals(that.elements) &&
                subCollections.equals(that.subCollections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elements, subCollections);
    }
}
