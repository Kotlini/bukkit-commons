package fr.kotlini.commons.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

class CachedDataStorageTest {

    private static final Logger LOGGER = Logger.getLogger(CachedDataStorageTest.class.getName());

    private FakePersistence persistence;
    private ScheduledExecutorService executor;
    private CachedDataStorage<TestData> cached;

    @BeforeEach
    void setUp() {
        persistence = new FakePersistence();
        executor = Executors.newSingleThreadScheduledExecutor();
        cached = new CachedDataStorage<>(persistence, executor, id -> new TestData(id, 0), LOGGER);
    }

    @Test
    void testGetReturnCachedValueAfterSave() {
        cached.save(new TestData("p1", 100));
        assertThat(cached.get("p1").score).isEqualTo(100);
    }

    @Test
    void testGetReturnValueFromPersistenceOnCacheMiss() {
        persistence.data.put("p1", new TestData("p1", 50));
        assertThat(cached.get("p1").score).isEqualTo(50);
    }

    @Test
    void testGetReturnNullWhenNotInCacheNorPersistence() {
        CachedDataStorage<TestData> noBuild = new CachedDataStorage<>(persistence, executor, null, LOGGER);
        assertThat(noBuild.get("unknown")).isNull();
    }

    @Test
    void testGetReturnCachedValueOnSecondCallWithoutHittingPersistence() {
        persistence.data.put("p1", new TestData("p1", 50));

        cached.get("p1");
        persistence.data.clear();

        assertThat(cached.get("p1").score).isEqualTo(50);
    }

    @Test
    void testLoadOrCreateReturnExistingDataFromPersistence() {
        persistence.data.put("p1", new TestData("p1", 50));

        TestData result = cached.loadOrCreate("p1");
        assertThat(result.score).isEqualTo(50);
    }

    @Test
    void testLoadOrCreateReturnNewDataWhenNotInPersistence() {
        TestData result = cached.loadOrCreate("p1");
        assertThat(result).isNotNull();
        assertThat(result.id).isEqualTo("p1");
        assertThat(result.score).isEqualTo(0);
    }

    @Test
    void testLoadOrCreateReturnNullWhenBuilderIsNull() {
        CachedDataStorage<TestData> noBuild = new CachedDataStorage<>(persistence, executor, null, LOGGER);
        assertThat(noBuild.loadOrCreate("p1")).isNull();
    }

    @Test
    void testLoadOrCreateSaveNewDataToPersistence() {
        cached.loadOrCreate("p1");
        assertThat(persistence.data).containsKey("p1");
    }

    @Test
    void testDeleteRemoveFromCache() {
        cached.save(new TestData("p1", 100));
        cached.delete("p1");
        assertThat(cached.getCache()).doesNotContainKey("p1");
    }

    @Test
    void testClearEmptyTheCacheAndPersistence() throws Exception {
        cached.save(new TestData("p1", 100));
        cached.save(new TestData("p2", 200));
        executor.submit(() -> {}).get();
        cached.clear();
        assertThat(cached.getCache()).isEmpty();
        assertThat(persistence.data).isEmpty();
    }

    @Test
    void testStopFlushPendingWritesAndClearCache() {
        cached.save(new TestData("p1", 100));
        cached.stop();
        assertThat(cached.getCache()).isEmpty();
    }

    @RepeatedTest(500)
    void testConcurrentLoadOrCreateBuildsOnlyOnce() throws Exception {
        AtomicInteger buildCount = new AtomicInteger();
        CachedDataStorage<TestData> storage = new CachedDataStorage<>(persistence, executor, id -> {
            buildCount.incrementAndGet();
            return new TestData(id, 0);
        }, LOGGER);

        int count = 16;
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(count);
        CyclicBarrier barrier = new CyclicBarrier(count);
        List<Future<TestData>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(pool.submit(() -> {
                barrier.await();
                return storage.loadOrCreate("p1");
            }));
        }

        List<TestData> results = new ArrayList<>();
        for (Future<TestData> future : futures) {
            results.add(future.get());
        }
        pool.shutdown();
        pool.close();

        assertThat(buildCount.get()).isEqualTo(1);
        TestData expected = results.getFirst();
        for (TestData result : results) {
            assertThat(result).isSameAs(expected);
        }
    }

    @Test
    void testStartDelegateToPersistence() {
        FakePersistence spy = new FakePersistence();
        CachedDataStorage<TestData> storage = new CachedDataStorage<>(spy, executor, null, LOGGER);
        storage.start();
        assertThat(spy.started).isTrue();
    }


    static class FakePersistence implements IDataStorage<TestData> {

        final Map<String, TestData> data = new HashMap<>();
        boolean started = false;

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public TestData save(TestData d) {
            data.put(d.getId(), d);
            return d;
        }

        @Override
        public TestData get(String id) {
            return data.get(id);
        }

        @Override
        public void delete(String id) {
            data.remove(id);
        }

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void stop() {}
    }
    static class TestData implements IIdentifiable {
        String id;
        int score;

        TestData(String id, int score) {
            this.id = id;
            this.score = score;
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
