package fr.kotlini.commons.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record YamlDataReader(ConfigurationSection section, String fileName) implements IDataReader {

    @Override
    public int readInt(String key) {
        return section.getInt(key);
    }

    @Override
    public boolean readBoolean(String key) {
        return section.getBoolean(key);
    }

    @Override
    public long readLong(String key) {
        return section.getLong(key);
    }

    @Override
    public String readString(String key) {
        return section.getString(key);
    }

    @Override
    public float readFloat(String key) {
        return (float) section.getDouble(key);
    }

    @Override
    public double readDouble(String key) {
        return section.getDouble(key);
    }

    @Override
    public boolean has(String key) {
        return section.contains(key);
    }

    @Override
    public Set<String> getKeys() {
        return section.getKeys(false);
    }

    @Override
    public IDataReader getSection(String key) {
        ConfigurationSection sub = section.getConfigurationSection(key);
        return sub != null ? new YamlDataReader(sub, this.fileName) : null;
    }

    @Override
    public List<String> readStringList(String key) {
        return section.getStringList(key);
    }

    @Override
    public List<IDataReader> getSectionList(String key) {
        ConfigurationSection listSection = section.getConfigurationSection(key);
        if (listSection != null) {
            List<IDataReader> readers = new ArrayList<>();
            for (String subKey : listSection.getKeys(false)) {
                ConfigurationSection sub = listSection.getConfigurationSection(subKey);
                if (sub != null) {
                    readers.add(new YamlDataReader(sub, this.fileName));
                }
            }
            return readers;
        }

        List<Map<?, ?>> mapList = section.getMapList(key);
        if (!mapList.isEmpty()) {
            List<IDataReader> readers = new ArrayList<>();
            for (Map<?, ?> map : mapList) {
                MemoryConfiguration mem = new MemoryConfiguration();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    mem.set(entry.getKey().toString(), entry.getValue());
                }
                readers.add(new YamlDataReader(mem, this.fileName));
            }
            return readers;
        }

        return Collections.emptyList();
    }

    @Override
    public String getPath() {
        String path = section.getCurrentPath();
        if (fileName != null && !fileName.isEmpty()) {
            return path.isEmpty() ? fileName : fileName + " > " + path;
        }
        return path;
    }
}
