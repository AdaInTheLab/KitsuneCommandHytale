package com.kitsunecommand.data.repositories;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.logging.HytaleLogger;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data access for the settings key-value table.
 * Feature settings are stored in the database (same pattern as the C# version).
 */
public class SettingsRepository {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Jdbi jdbi;

    @Inject
    public SettingsRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Get a setting value by key.
     */
    public Optional<String> get(String key) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT value FROM settings WHERE name = :name")
                .bind("name", key)
                .mapTo(String.class)
                .findOne()
        );
    }

    /**
     * Get a setting value with a default fallback.
     */
    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /**
     * Get a setting as an integer.
     */
    public int getInt(String key, int defaultValue) {
        return get(key).map(v -> {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }).orElse(defaultValue);
    }

    /**
     * Get a setting as a boolean.
     */
    public boolean getBool(String key, boolean defaultValue) {
        return get(key).map(v ->
            "true".equalsIgnoreCase(v) || "1".equals(v)
        ).orElse(defaultValue);
    }

    /**
     * Set (upsert) a setting value.
     */
    public void set(String key, String value) {
        jdbi.useHandle(handle ->
            handle.execute(
                "INSERT INTO settings (name, value) VALUES (?, ?) ON CONFLICT(name) DO UPDATE SET value = ?",
                key, value, value
            )
        );
    }

    /**
     * Set a setting as an integer.
     */
    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    /**
     * Set a setting as a boolean.
     */
    public void setBool(String key, boolean value) {
        set(key, value ? "true" : "false");
    }

    /**
     * Delete a setting.
     */
    public void delete(String key) {
        jdbi.useHandle(handle ->
            handle.execute("DELETE FROM settings WHERE name = ?", key)
        );
    }

    /**
     * Get all settings matching a prefix (e.g., "points." for all points settings).
     */
    public Map<String, String> getAllByPrefix(String prefix) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT name, value FROM settings WHERE name LIKE :prefix")
                .bind("prefix", prefix + "%")
                .map((rs, ctx) -> Map.entry(rs.getString("name"), rs.getString("value")))
                .list()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}
