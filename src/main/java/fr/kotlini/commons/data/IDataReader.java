package fr.kotlini.commons.data;

import fr.kotlini.commons.config.ConfigException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface IDataReader {

    int readInt(String key);

    boolean readBoolean(String key);

    long readLong(String key);

    String readString(String key);

    float readFloat(String key);

    double readDouble(String key);

    boolean has(String key);

    Set<String> getKeys();

    IDataReader getSection(String key);

    List<String> readStringList(String key);

    List<IDataReader> getSectionList(String key);

    String getPath();

    default int readInt(String key, int def) {
        return has(key) ? readInt(key) : def;
    }

    default long readLong(String key, long def) {
        return has(key) ? readLong(key) : def;
    }

    default float readFloat(String key, float def) {
        return has(key) ? readFloat(key) : def;
    }

    default double readDouble(String key, double def) {
        return has(key) ? readDouble(key) : def;
    }

    default boolean readBoolean(String key, boolean def) {
        return has(key) ? readBoolean(key) : def;
    }

    default String readString(String key, String def) {
        return has(key) ? readString(key) : def;
    }

    default <E extends Enum<E>> E readEnum(String key, Class<E> enumClass) {
        String raw = readString(key);
        if (raw == null || raw.isEmpty()) {
            throw new ConfigException(getPath(),
                    "missing value for key '" + key + "'");
        }

        try {
            return Enum.valueOf(enumClass, raw.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new ConfigException(getPath() + "." + key,
                    "'" + raw + "' is not a valid " + enumClass.getSimpleName()
                            + ". Expected: " + Arrays.toString(enumClass.getEnumConstants()));
        }
    }

    default <E extends Enum<E>> E readEnum(String key, Class<E> enumClass, E def) {
        return has(key) ? readEnum(key, enumClass) : def;
    }

    default <E extends Enum<E>> EnumSet<E> readEnumSet(String key, Class<E> enumClass) {
        List<String> names = readStringList(key);
        if (names.isEmpty()) return EnumSet.noneOf(enumClass);

        EnumSet<E> set = EnumSet.noneOf(enumClass);
        List<String> invalid = new ArrayList<>();

        for (String name : names) {
            try {
                set.add(Enum.valueOf(enumClass, name.toUpperCase().replace("-", "_")));
            } catch (IllegalArgumentException e) {
                invalid.add(name);
            }
        }

        if (!invalid.isEmpty()) {
            throw new ConfigException(getPath() + "." + key,
                    "invalid " + enumClass.getSimpleName() + " values: " + invalid
                            + ". Expected: " + Arrays.toString(enumClass.getEnumConstants()));
        }

        return set;
    }

    default String requireString(String key) {
        if (!has(key)) {
            throw new ConfigException(getPath(), "missing required key '" + key + "'");
        }
        return readString(key);
    }

    default IDataReader requireSection(String key) {
        IDataReader section = getSection(key);
        if (section == null) {
            throw new ConfigException(getPath(), "missing required section '" + key + "'");
        }
        return section;
    }
}
