package fr.kotlini.commons.factory;

import fr.kotlini.commons.data.IDataReader;

@FunctionalInterface
public interface IFactory<T> {

    T create(String id, IDataReader reader);
}
