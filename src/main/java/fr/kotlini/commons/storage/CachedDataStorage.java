package fr.kotlini.commons.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedDataStorage<T extends IIdentifiable> implements IDataStorage<T> {

    private final IDataStorage<T> persistence;
    private final ScheduledExecutorService executor;
    private final Function<String, T> builder;
    private final Map<String, T> cache;
    private final Logger logger;

    public CachedDataStorage(
            IDataStorage<T> persistence,
            ScheduledExecutorService executor,
            Function<String, T> builder,
            Logger logger) {
        this.persistence = persistence;
        this.executor = executor;
        this.builder = builder;
        this.logger = logger;
        this.cache = new ConcurrentHashMap<>();
    }

    public T loadOrCreate(String id) {
        return this.cache.computeIfAbsent(id, k -> {
            T data = this.persistence.get(k);
            if (data == null && this.builder != null) {
                data = this.builder.apply(k);
                if (data != null) {
                    try {
                        this.persistence.save(data);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to persist new entry for id " + k, e);
                    }
                }
            }
            return data;
        });
    }

    @Override
    public void clear() {
        this.cache.clear();
        this.persistence.clear();
    }

    @Override
    public T save(T data) {
        this.cache.put(data.getId(), data);
        this.executor.execute(() -> {
            try {
                this.persistence.save(data);
            } catch (Exception e) {
                this.cache.remove(data.getId());
                logger.log(Level.SEVERE, "Failed to persist save for id " + data.getId(), e);
            }
        });
        return data;
    }

    @Override
    public T get(String id) {
        T cached = this.cache.get(id);
        if (cached != null) return cached;

        T data = this.persistence.get(id);
        if (data == null) return null;

        T existing = this.cache.putIfAbsent(id, data);
        return existing != null ? existing : data;
    }

    @Override
    public void delete(String id) {
        this.cache.remove(id);
        this.executor.execute(() -> {
            try {
                this.persistence.delete(id);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to persist delete for id " + id, e);
            }
        });
    }

    @Override
    public void start() {
        this.persistence.start();
    }

    @Override
    public void stop() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                List<Runnable> remaining = this.executor.shutdownNow();
                remaining.forEach(Runnable::run);
            }
        } catch (InterruptedException e) {
            List<Runnable> remaining = this.executor.shutdownNow();
            remaining.forEach(Runnable::run);
            Thread.currentThread().interrupt();
        }
        this.persistence.stop();
        this.cache.clear();
    }

    public Map<String, T> getCache() {
        return Collections.unmodifiableMap(cache);
    }
}
