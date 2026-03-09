package fr.kotlini.commons.storage;

public interface IDataStorage<T extends IIdentifiable> {

    void clear();

    T save(T data);

    T get(String id);

    void delete(String id);

    void start();

    void stop();
}
