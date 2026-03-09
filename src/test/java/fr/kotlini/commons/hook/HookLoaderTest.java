package fr.kotlini.commons.hook;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HookLoaderTest {

    private Plugin plugin;
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        pluginManager = mock(PluginManager.class);
        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(server.getPluginManager()).thenReturn(pluginManager);
    }

    @Test
    void testLoadReturnHookWhenPluginIsPresent() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("Vault")).thenReturn(target);

        String result = HookLoader.load(plugin, "Vault", () -> "hook", () -> "fallback");
        assertThat(result).isEqualTo("hook");
    }

    @Test
    void testLoadReturnFallbackWhenPluginIsAbsent() {
        when(pluginManager.getPlugin("Vault")).thenReturn(null);

        String result = HookLoader.load(plugin, "Vault", () -> "hook", () -> "fallback");
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void testLoadReturnFallbackWhenPluginIsDisabled() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(false);
        when(pluginManager.getPlugin("Vault")).thenReturn(target);

        String result = HookLoader.load(plugin, "Vault", () -> "hook", () -> "fallback");
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void testLoadReturnFallbackWhenHookSupplierThrowsException() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("Vault")).thenReturn(target);

        String result = HookLoader.load(plugin, "Vault",
                () -> { throw new RuntimeException("broken"); },
                () -> "fallback");
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void testLoadReturnFallbackWhenHookSupplierThrowsNoClassDefFoundError() {
        Plugin target = mock(Plugin.class);
        when(target.isEnabled()).thenReturn(true);
        when(pluginManager.getPlugin("Vault")).thenReturn(target);

        String result = HookLoader.load(plugin, "Vault",
                () -> { throw new NoClassDefFoundError("missing"); },
                () -> "fallback");
        assertThat(result).isEqualTo("fallback");
    }
}
