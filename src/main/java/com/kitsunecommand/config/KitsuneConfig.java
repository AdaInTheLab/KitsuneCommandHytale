package com.kitsunecommand.config;

/**
 * Top-level plugin configuration.
 * Values are stored in the plugin's config.json and loaded via Hytale's BuilderCodec.
 *
 * For Phase 1, we keep it simple with just the essentials.
 * Feature-specific settings are stored in the database (same pattern as the C# version).
 */
public class KitsuneConfig {

    private String databaseFile = "kitsunecommand.db";
    private int webPort = 8888;
    private boolean webEnabled = false; // Phase 3
    private String serverName = "Hytale Server";

    // --- Getters & Setters ---

    public String getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(String databaseFile) {
        this.databaseFile = databaseFile;
    }

    public int getWebPort() {
        return webPort;
    }

    public void setWebPort(int webPort) {
        this.webPort = webPort;
    }

    public boolean isWebEnabled() {
        return webEnabled;
    }

    public void setWebEnabled(boolean webEnabled) {
        this.webEnabled = webEnabled;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
