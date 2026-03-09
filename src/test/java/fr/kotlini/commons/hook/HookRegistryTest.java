package fr.kotlini.commons.hook;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HookRegistryTest {

    private PluginManager pluginManager;
    private HookRegistry registry;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        Logger logger = Logger.getLogger("test");

        registry = new HookRegistry(pluginManager, logger);
    }

    @Test
    void testRegisterAndGetHookReturnTypedList() {
        DummyHook hook = new DummyHook();
        registry.register(IHook.class, hook);

        List<IHook> hooks = registry.getHooks(IHook.class);
        assertThat(hooks).containsExactly(hook);
    }

    @Test
    void testGetHooksReturnEmptyListWhenNoneRegistered() {
        assertThat(registry.getHooks(IHook.class)).isEmpty();
    }

    @Test
    void testRegisterMultipleHooksOfSameType() {
        DummyHook hook1 = new DummyHook();
        DummyHook hook2 = new DummyHook();
        registry.register(IHook.class, hook1);
        registry.register(IHook.class, hook2);

        assertThat(registry.getHooks(IHook.class)).containsExactly(hook1, hook2);
    }

    @Test
    void testTryRegisterSucceedWhenPluginIsPresent() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("WorldGuard")).thenReturn(target);

        registry.tryRegister("WorldGuard", IHook.class, DummyHook::new);

        assertThat(registry.getHooks(IHook.class)).hasSize(1);
    }

    @Test
    void testTryRegisterSkipWhenPluginIsAbsent() {
        when(pluginManager.getPlugin("WorldGuard")).thenReturn(null);

        registry.tryRegister("WorldGuard", IHook.class, DummyHook::new);

        assertThat(registry.getHooks(IHook.class)).isEmpty();
    }

    @Test
    void testTryRegisterSkipWhenPluginIsDisabled() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(false);
        when(pluginManager.getPlugin("WorldGuard")).thenReturn(target);

        registry.tryRegister("WorldGuard", IHook.class, DummyHook::new);

        assertThat(registry.getHooks(IHook.class)).isEmpty();
    }

    @Test
    void testTryRegisterSkipWhenFactoryThrowsException() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("WorldGuard")).thenReturn(target);

        registry.tryRegister("WorldGuard", IHook.class,
                () -> { throw new RuntimeException("broken"); });

        assertThat(registry.getHooks(IHook.class)).isEmpty();
    }

    @Test
    void testTryRegisterWithPluginFunctionPassesPluginInstance() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("Vault")).thenReturn(target);

        registry.tryRegister("Vault", IHook.class, p -> {
            assertThat(p).isSameAs(target);
            return new DummyHook();
        });

        assertThat(registry.getHooks(IHook.class)).hasSize(1);
    }

    @Test
    void testGetHooksReturnDifferentListsForDifferentTypes() {
        DummyHook hook = new DummyHook();
        OtherHook other = new OtherHook();
        registry.register(IHook.class, hook);
        registry.register(IOther.class, other);

        assertThat(registry.getHooks(IHook.class)).containsExactly(hook);
        assertThat(registry.getHooks(IOther.class)).containsExactly(other);
    }

    interface IHook {}
    interface IOther {}
    static class DummyHook implements IHook {}
    static class OtherHook implements IOther {}
}
