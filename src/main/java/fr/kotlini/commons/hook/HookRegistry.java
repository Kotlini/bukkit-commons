package fr.kotlini.commons.hook;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HookRegistry {

    private final Map<Class<?>, List<Object>> hooks;
    private final PluginManager pluginManager;
    private final Logger logger;

    public HookRegistry(PluginManager pluginManager, Logger logger) {
        this.pluginManager = pluginManager;
        this.logger = logger;
        this.hooks = new HashMap<>();
    }

    public <T> void register(Class<T> type, T hook) {
        hooks.computeIfAbsent(type, k -> new ArrayList<>()).add(hook);
    }

    public <T> void tryRegister(String pluginName, Class<T> type, Supplier<T> factory) {
        tryRegister(pluginName, type, p -> factory.get());
    }

    public <T> void tryRegister(String pluginName, Class<T> type, Function<Plugin, T> factory) {
        Plugin target = pluginManager.getPlugin(pluginName);
        if (target == null || !target.isEnabled()) return;

        try {
            T hook = factory.apply(target);
            if (hook != null) {
                register(type, hook);
                logger.info("Hooked into " + pluginName);
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to hook into " + pluginName, t);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getHooks(Class<T> type) {
        return Collections.unmodifiableList((List<T>) hooks.getOrDefault(type, Collections.emptyList()));
    }
}
