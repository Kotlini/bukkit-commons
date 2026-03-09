package fr.kotlini.commons.config;

import fr.kotlini.commons.data.IDataReader;
import fr.kotlini.commons.data.YamlDataReader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public abstract class YamlConfigLoader  {

    protected final Plugin plugin;
    protected final String fileName;
    protected final File configFile;
    protected IDataReader dataReader;

    public YamlConfigLoader(Plugin plugin, String configName) {
        this.plugin = plugin;
        this.fileName = configName + ".yml";
        this.configFile = new File(plugin.getDataFolder(), this.fileName);
    }

    protected abstract void loadConfig();

    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        this.dataReader = new YamlDataReader(YamlConfiguration.loadConfiguration(configFile), fileName);
        loadConfig();
    }

    protected Logger getLogger() {
        return plugin.getLogger();
    }

    protected <V> Map<String, V> loadSection(
            String sectionId,
            BiFunction<String, IDataReader, V> mapper) {
        IDataReader section = this.dataReader.getSection(sectionId);
        if (section == null) return Collections.emptyMap();

        return loadSection(section, mapper);
    }

    protected <V> Map<String, V> loadSection(
            IDataReader section,
            BiFunction<String, IDataReader, V> mapper) {
        return loadEntries(section, mapper, Function.identity());
    }

    protected <V> Map<String, V> loadSection(
            String sectionId,
            IDataReader parent,
            BiFunction<String, IDataReader, V> mapper) {
        IDataReader section = parent.getSection(sectionId);
        if (section == null) return Collections.emptyMap();

        return loadSection(section, mapper);
    }

    protected <K, V> Map<K, V> loadSectionMapped(
            String sectionId,
            BiFunction<String, IDataReader, V> mapper,
            Function<String, K> keyParser) {
        IDataReader section = this.dataReader.getSection(sectionId);
        if (section == null) return Collections.emptyMap();
        return loadEntries(section, mapper, keyParser);
    }

    protected <V> V loadSingle(
            String sectionId,
            BiFunction<String, IDataReader, V> mapper) {
        IDataReader section = this.dataReader.getSection(sectionId);
        if (section == null) return null;

        return mapper.apply(sectionId, section);
    }

    protected <V> V loadSingle(
            String sectionId,
            IDataReader parent,
            BiFunction<String, IDataReader, V> mapper) {
        IDataReader section = parent.getSection(sectionId);
        if (section == null) return null;

        return mapper.apply(sectionId, section);
    }

    private <K, V> Map<K, V> loadEntries(
            IDataReader section,
            BiFunction<String, IDataReader, V> mapper,
            Function<String, K> keyParser) {
        Set<String> keys = section.getKeys();
        Map<K, V> result = new HashMap<>(keys.size());

        for (String key : keys) {
            IDataReader sub = section.getSection(key);
            if (sub == null) continue;

            try {
                K parsedKey = keyParser.apply(key);
                V value = mapper.apply(key, sub);
                if (value != null) result.put(parsedKey, value);
            } catch (ConfigException | IllegalArgumentException e) {
                getLogger().warning(e.getMessage());
            }
        }

        return result;
    }
}
