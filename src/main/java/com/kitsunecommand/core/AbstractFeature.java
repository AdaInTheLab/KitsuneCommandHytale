package com.kitsunecommand.core;

import com.hypixel.hytale.logger.HytaleLogger;
import com.kitsunecommand.data.repositories.SettingsRepository;

import java.util.logging.Level;

/**
 * Base class for all KitsuneCommand features.
 * Replaces FeatureBase<TSettings> from the C# version.
 *
 * Each feature has:
 * - A unique name/key used for settings prefix
 * - An enabled/disabled state persisted in the settings table
 * - Lifecycle hooks: onEnable(), onDisable(), onStart(), onShutdown()
 * - Access to the settings repository for reading feature-specific config
 */
public abstract class AbstractFeature {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final String featureKey;
    private final String displayName;
    private boolean enabled;

    protected AbstractFeature(String featureKey, String displayName) {
        this.featureKey = featureKey;
        this.displayName = displayName;
        this.enabled = false;
    }

    /**
     * Called during setup phase to initialize and register event listeners.
     * Features should register their event handlers here.
     */
    public abstract void onEnable();

    /**
     * Called during start phase after all features are enabled.
     * Features can start scheduled tasks, interact with other features, etc.
     */
    public void onStart() {
        // Default no-op — override if needed
    }

    /**
     * Called during shutdown to clean up resources.
     */
    public void onShutdown() {
        // Default no-op — override if needed
    }

    /**
     * Load whether this feature is enabled from the settings database.
     * Default is true if no setting exists.
     */
    public void loadEnabledState(SettingsRepository settings) {
        this.enabled = settings.getBool(featureKey + ".enabled", true);
    }

    // --- Accessors ---

    public String getFeatureKey() {
        return featureKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Helper to build a settings key for this feature.
     * e.g., for feature "points" and setting "killReward": "points.killReward"
     */
    protected String settingsKey(String setting) {
        return featureKey + "." + setting;
    }

    @Override
    public String toString() {
        return displayName + " [" + featureKey + "] " + (enabled ? "ENABLED" : "DISABLED");
    }
}
