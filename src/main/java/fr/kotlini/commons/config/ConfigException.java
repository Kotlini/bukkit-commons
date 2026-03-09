package fr.kotlini.commons.config;

public class ConfigException extends RuntimeException {

    public ConfigException(String path, String message) {
        super("[" + path + "] " + message);
    }
}
