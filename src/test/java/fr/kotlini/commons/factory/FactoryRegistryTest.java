package fr.kotlini.commons.factory;

import fr.kotlini.commons.data.IDataReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public final class FactoryRegistryTest {

    private FactoryRegistry<String> registry;
    private IDataReader reader;

    @BeforeEach
    void setUp() {
        registry = new FactoryRegistry<>();
        reader = mock(IDataReader.class);
    }

    @Test
    void testCreateReturnEmptyWhenIdIsNotRegistered() {
        Optional<String> result = registry.create("unknown", reader);

        assertThat(result).isEmpty();
    }

    @Test
    void testCreateReturnValueWhenFactoryIsRegistered() {
        registry.register("sword", (id, r) -> "diamond_sword");

        Optional<String> result = registry.create("sword", reader);

        assertThat(result).hasValue("diamond_sword");
    }

    @Test
    void testCreatePassesIdAndReaderToFactory() {
        registry.register("item", (id, r) -> id + ":" + r.hashCode());

        Optional<String> result = registry.create("item", reader);

        assertThat(result).hasValue("item:" + reader.hashCode());
    }

    @Test
    void testCreateReturnEmptyWhenFactoryReturnsNull() {
        registry.register("nullable", (id, r) -> null);

        Optional<String> result = registry.create("nullable", reader);

        assertThat(result).isEmpty();
    }

    @Test
    void testRegisterOverwritesPreviousFactory() {
        registry.register("item", (id, r) -> "old");
        registry.register("item", (id, r) -> "new");

        Optional<String> result = registry.create("item", reader);

        assertThat(result).hasValue("new");
    }

    @Test
    void testCreateIsolatesFactoriesByKey() {
        registry.register("a", (id, r) -> "valueA");
        registry.register("b", (id, r) -> "valueB");

        assertThat(registry.create("a", reader)).hasValue("valueA");
        assertThat(registry.create("b", reader)).hasValue("valueB");
    }
}
