package fr.kotlini.commons.factory;

import fr.kotlini.commons.data.IDataReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class FactoryRegistry<T> {

    private final Map<String, IFactory<T>> factories = new HashMap<>();

    public void register(String id, IFactory<T> factory) {
        factories.put(id, factory);
    }

    public Optional<T> create(String id, IDataReader reader) {
        IFactory<T> factory = factories.get(id);
        return factory != null ? Optional.ofNullable(factory.create(id, reader)) : Optional.empty();
    }
}
