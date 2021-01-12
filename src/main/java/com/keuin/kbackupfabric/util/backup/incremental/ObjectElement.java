package com.keuin.kbackupfabric.util.backup.incremental;

import com.keuin.kbackupfabric.util.backup.incremental.identifier.ObjectIdentifier;

import java.util.Objects;

/**
 * Representing a file in a ObjectCollection.
 * Immutable.
 */
public class ObjectElement {
    private final String name;
    private final ObjectIdentifier identifier;

    public ObjectElement(String name, ObjectIdentifier identifier) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(identifier);
        this.name = name;
        this.identifier = identifier;
    }

    /**
     * Get file name.
     * @return the file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get file identifier, which is considered to be different between files with different contents.
     * @return the identifier.
     */
    public ObjectIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectElement that = (ObjectElement) o;
        return name.equals(that.name) &&
                identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, identifier);
    }

    @Override
    public String toString() {
        return "ObjectElement{" +
                "name='" + name + '\'' +
                ", identifier=" + identifier +
                '}';
    }
}
