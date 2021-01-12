package com.keuin.kbackupfabric.util.backup.incremental;

import com.keuin.kbackupfabric.util.backup.incremental.identifier.ObjectIdentifier;

import java.util.Objects;
import java.util.Set;

public class ObjectCollection {
    private final String name;
    private final Set<ObjectIdentifier> elements;
    private final Set<ObjectCollection> subCollections;

    ObjectCollection(String name, Set<ObjectIdentifier> elements, Set<ObjectCollection> subCollections) {
        this.name = Objects.requireNonNull(name);
        this.elements = Objects.requireNonNull(elements);
        this.subCollections = Objects.requireNonNull(subCollections);
    }

    public String getName() {
        return name;
    }

    public Set<ObjectIdentifier> getElements() {
        return elements;
    }

    public Set<ObjectCollection> getSubCollections() {
        return subCollections;
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
