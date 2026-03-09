package fr.kotlini.commons.storage;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class JsonDataStorageTest {

    @TempDir
    Path tempDir;

    private JsonDataStorage<TestData> storage;

    @BeforeEach
    void setUp() {
        storage = new JsonDataStorage<>(tempDir.toFile(), TestData.class);
        storage.start();
    }

    @Test
    void testSaveShouldCreateJsonFile() {
        storage.save(new TestData("player1", 100));

        File file = tempDir.resolve("player1.json").toFile();
        assertThat(file).exists();
    }

    @Test
    void testSaveShouldWriteValidJson() throws IOException {
        storage.save(new TestData("player1", 100));

        String json = Files.readString(tempDir.resolve("player1.json"));
        TestData parsed = new Gson().fromJson(json, TestData.class);
        assertThat(parsed.id).isEqualTo("player1");
        assertThat(parsed.score).isEqualTo(100);
    }

    @Test
    void testGetReturnSavedData() {
        storage.save(new TestData("player1", 100));

        TestData result = storage.get("player1");
        assertThat(result).isNotNull();
        assertThat(result.id).isEqualTo("player1");
        assertThat(result.score).isEqualTo(100);
    }

    @Test
    void testGetReturnNullWhenFileDoesNotExist() {
        assertThat(storage.get("unknown")).isNull();
    }

    @Test
    void testGetReturnNullWhenFileIsCorrupted() throws IOException {
        Files.writeString(tempDir.resolve("bad.json"), "{invalid json!!");

        assertThat(storage.get("bad")).isNull();
    }

    @Test
    void testDeleteShouldRemoveFile() {
        storage.save(new TestData("player1", 100));
        storage.delete("player1");

        File file = tempDir.resolve("player1.json").toFile();
        assertThat(file).doesNotExist();
    }

    @Test
    void testDeleteShouldNotThrowWhenFileDoesNotExist() {
        assertThatCode(() -> storage.delete("unknown")).doesNotThrowAnyException();
    }

    @Test
    void testClearShouldDeleteAllJsonFiles() {
        storage.save(new TestData("player1", 100));
        storage.save(new TestData("player2", 200));
        storage.clear();

        File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertThat(files).isEmpty();
    }

    @Test
    void testClearShouldNotThrowOnEmptyFolder() {
        assertThatCode(() -> storage.clear()).doesNotThrowAnyException();
    }

    @Test
    void testSaveShouldOverwriteExistingData() {
        storage.save(new TestData("player1", 100));
        storage.save(new TestData("player1", 999));

        TestData result = storage.get("player1");
        assertThat(result.score).isEqualTo(999);
    }

    @Test
    void testSaveShouldRejectIdWithPathTraversal() {
        assertThatThrownBy(() -> storage.save(new TestData("../etc", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetShouldRejectIdWithPathSeparator() {
        assertThatThrownBy(() -> storage.get("foo/bar"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testStartShouldCreateFolderIfNotExists() {
        File subFolder = tempDir.resolve("nested").toFile();
        JsonDataStorage<TestData> nested = new JsonDataStorage<>(subFolder, TestData.class);
        nested.start();

        assertThat(subFolder).exists().isDirectory();
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
