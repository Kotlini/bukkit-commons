package fr.kotlini.commons.storage;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonDataStorage<T extends IIdentifiable> implements IDataStorage<T> {

    private final File folder;
    private final Gson gson;
    private final Class<T> dataType;
    private final Logger logger;

    public JsonDataStorage(File folder, Class<T> dataType) {
        this(folder, dataType, new Gson());
    }

    public JsonDataStorage(File folder, Class<T> dataType, Gson gson) {
        this(folder, dataType, gson, Logger.getLogger(JsonDataStorage.class.getName()));
    }

    public JsonDataStorage(File folder, Class<T> dataType, Gson gson, Logger logger) {
        this.folder = folder;
        this.gson = gson;
        this.dataType = dataType;
        this.logger = logger;
    }

    @Override
    public void clear() {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                file.delete();
            }
        }
    }

    @Override
    public T save(T data) {
        File file = getFile(data.getId());

        try {
            Files.writeString(file.toPath(), gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + file.getAbsolutePath(), e);
        }

        return data;
    }

    @Override
    public T get(String id) {
        File file = getFile(id);
        if (!file.exists()) return null;

        try {
            return gson.fromJson(Files.readString(file.toPath()), dataType);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to read " + file.getAbsolutePath(), e);
            return null;
        }
    }

    @Override
    public void delete(String id) {
        File file = getFile(id);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void start() {
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public void stop() {}

    private File getFile(String id) {
        if (id.contains("..") || id.contains("/") || id.contains("\\")) {
            throw new IllegalArgumentException("Invalid storage id: " + id);
        }
        return new File(folder, id + ".json");
    }
}
