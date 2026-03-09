package fr.kotlini.commons.hook;

import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;
import java.util.logging.Level;

public final class HookLoader {

    private HookLoader() {
        throw new UnsupportedOperationException();
    }

    public static <T> T load(
            Plugin plugin,
            String targetPlugin,
            Supplier<T> hookSupplier,
            Supplier<T> fallbackSupplier) {
        Plugin found = plugin.getServer().getPluginManager().getPlugin(targetPlugin);

        if (found == null || !found.isEnabled()) {
            return fallbackSupplier.get();
        }

        try {
            T hook = hookSupplier.get();
            plugin.getLogger().info("Hooked into " + targetPlugin);
            return hook;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to hook into " + targetPlugin + ", using fallback", t);

            return fallbackSupplier.get();
        }
    }
}
