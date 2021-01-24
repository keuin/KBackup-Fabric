package com.keuin.kbackupfabric.backup.incremental;

import java.io.Serializable;
import java.util.*;

public class ObjectCollection2 implements Serializable {

    private static final long serialVersionUID = 6651743898782813296L;
    private final String name;
    private final Map<String, ObjectElement> elements;
    private final Map<String, ObjectCollection2> subCollections;

    ObjectCollection2(String name, Set<ObjectElement> elements, Map<String, ObjectCollection2> subCollections) {
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

    public Set<ObjectCollection2> getSubCollectionSet() {
        return new HashSet<>(subCollections.values());
    }

    public Map<String, ObjectCollection2> getSubCollectionMap() {
        return Collections.unmodifiableMap(subCollections);
    }

    public ObjectCollection2 getSubCollection(String name) {
        return subCollections.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectCollection2 that = (ObjectCollection2) o;
        return name.equals(that.name) &&
                elements.equals(that.elements) &&
                subCollections.equals(that.subCollections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elements, subCollections);
    }
}
