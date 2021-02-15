package com.keuin.kbackupfabric.backup.incremental;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class ObjectCollectionIterator implements Iterator<ObjectElement> {
    private Iterator<ObjectElement> currentIterator;
    private final List<ObjectCollection2> cols = new LinkedList<>();

    public ObjectCollectionIterator(ObjectCollection2 collection) {
        cols.addAll(collection.getSubCollectionSet());
        currentIterator = collection.getElementSet().iterator();
    }

    @Override
    public boolean hasNext() {
        if (currentIterator != null) {
            if (currentIterator.hasNext())
                return true;
            else {
                currentIterator = null;
                return hasNext();
            }
        } else {
            if (cols.isEmpty())
                return false;
            else {
                ObjectCollection2 consumedCollection = cols.remove(0);
                cols.addAll(consumedCollection.getSubCollectionSet());
                currentIterator = consumedCollection.getElementSet().iterator();
                return hasNext();
            }
        }
    }

    @Override
    public ObjectElement next() {
        if (hasNext()) {
            return currentIterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }

}
