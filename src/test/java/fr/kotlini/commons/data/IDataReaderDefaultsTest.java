package fr.kotlini.commons.data;

import fr.kotlini.commons.config.ConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class IDataReaderDefaultsTest {

    private StubDataReader reader;

    @BeforeEach
    void setUp() {
        reader = new StubDataReader();
    }

    @Test
    void testReadIntReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readInt("missing", 42)).isEqualTo(42);
    }

    @Test
    void testReadIntReturnValueWhenKeyExists() {
        reader.put("level", 10);
        assertThat(reader.readInt("level", 42)).isEqualTo(10);
    }

    @Test
    void testReadStringReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readString("missing", "fallback")).isEqualTo("fallback");
    }

    @Test
    void testReadStringReturnValueWhenKeyExists() {
        reader.put("name", "test");
        assertThat(reader.readString("name", "fallback")).isEqualTo("test");
    }

    @Test
    void testReadDoubleReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readDouble("missing", 3.14)).isEqualTo(3.14);
    }

    @Test
    void testReadBooleanReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readBoolean("missing", true)).isTrue();
    }

    @Test
    void testReadLongReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readLong("missing", 999L)).isEqualTo(999L);
    }

    @Test
    void testReadFloatReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readFloat("missing", 1.5f)).isEqualTo(1.5f);
    }

    @Test
    void testReadEnumReturnParsedValue() {
        reader.put("color", "RED");
        assertThat(reader.readEnum("color", Color.class)).isEqualTo(Color.RED);
    }

    @Test
    void testReadEnumHandleLowercaseAndDashes() {
        reader.put("color", "light-blue");
        assertThat(reader.readEnum("color", Color.class)).isEqualTo(Color.LIGHT_BLUE);
    }

    @Test
    void testReadEnumThrowConfigExceptionWhenValueIsInvalid() {
        reader.put("color", "PURPLE");
        assertThatThrownBy(() -> reader.readEnum("color", Color.class))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("PURPLE")
                .hasMessageContaining("Color");
    }

    @Test
    void testReadEnumThrowConfigExceptionWhenValueIsMissing() {
        assertThatThrownBy(() -> reader.readEnum("color", Color.class))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("missing value");
    }

    @Test
    void testReadEnumReturnDefaultWhenKeyIsMissing() {
        assertThat(reader.readEnum("color", Color.class, Color.GREEN)).isEqualTo(Color.GREEN);
    }

    @Test
    void testReadEnumReturnParsedValueWhenKeyExistsIgnoringDefault() {
        reader.put("color", "RED");
        assertThat(reader.readEnum("color", Color.class, Color.GREEN)).isEqualTo(Color.RED);
    }

    @Test
    void testReadEnumSetReturnParsedValues() {
        reader.putStringList("colors", List.of("RED", "green"));
        assertThat(reader.readEnumSet("colors", Color.class))
                .containsExactlyInAnyOrder(Color.RED, Color.GREEN);
    }

    @Test
    void testReadEnumSetReturnEmptySetWhenKeyIsMissing() {
        assertThat(reader.readEnumSet("colors", Color.class)).isEmpty();
    }

    @Test
    void testReadEnumSetThrowConfigExceptionWhenOneValueIsInvalid() {
        reader.putStringList("colors", List.of("RED", "INVALID"));
        assertThatThrownBy(() -> reader.readEnumSet("colors", Color.class))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("INVALID");
    }

    @Test
    void testRequireStringReturnValueWhenKeyExists() {
        reader.put("name", "test");
        assertThat(reader.requireString("name")).isEqualTo("test");
    }

    @Test
    void testRequireStringThrowConfigExceptionWhenKeyIsMissing() {
        assertThatThrownBy(() -> reader.requireString("name"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("missing required key")
                .hasMessageContaining("name");
    }

    @Test
    void testRequireSectionReturnSectionWhenKeyExists() {
        reader.putSection("sub", new StubDataReader());
        assertThat(reader.requireSection("sub")).isNotNull();
    }

    @Test
    void testRequireSectionThrowConfigExceptionWhenKeyIsMissing() {
        assertThatThrownBy(() -> reader.requireSection("sub"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("missing required section")
                .hasMessageContaining("sub");
    }

    enum Color {
        RED, GREEN, LIGHT_BLUE
    }

    static class StubDataReader implements IDataReader {

        private final java.util.Map<String, Object> data = new java.util.HashMap<>();

        void put(String key, Object value) {
            data.put(key, value);
        }

        void putStringList(String key, List<String> value) {
            data.put(key, value);
        }

        void putSection(String key, IDataReader section) {
            data.put(key, section);
        }

        @Override
        public int readInt(String key) {
            return data.containsKey(key) ? ((Number) data.get(key)).intValue() : 0;
        }

        @Override
        public boolean readBoolean(String key) {
            return data.containsKey(key) && (boolean) data.get(key);
        }

        @Override
        public long readLong(String key) {
            return data.containsKey(key) ? ((Number) data.get(key)).longValue() : 0L;
        }

        @Override
        public String readString(String key) {
            return data.containsKey(key) ? data.get(key).toString() : null;
        }

        @Override
        public float readFloat(String key) {
            return data.containsKey(key) ? ((Number) data.get(key)).floatValue() : 0f;
        }

        @Override
        public double readDouble(String key) {
            return data.containsKey(key) ? ((Number) data.get(key)).doubleValue() : 0d;
        }

        @Override
        public boolean has(String key) {
            return data.containsKey(key);
        }

        @Override
        public Set<String> getKeys() {
            return data.keySet();
        }

        @Override
        public IDataReader getSection(String key) {
            Object val = data.get(key);
            return val instanceof IDataReader ? (IDataReader) val : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> readStringList(String key) {
            Object val = data.get(key);
            return val instanceof List ? (List<String>) val : Collections.emptyList();
        }

        @Override
        public List<IDataReader> getSectionList(String key) {
            return Collections.emptyList();
        }

        @Override
        public String getPath() {
            return "test";
        }
    }
}
