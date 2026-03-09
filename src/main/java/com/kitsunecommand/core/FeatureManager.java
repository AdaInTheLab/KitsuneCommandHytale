package com.kitsunecommand.core;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.logging.HytaleLogger;
import com.kitsunecommand.data.repositories.SettingsRepository;
import com.kitsunecommand.features.economy.PointsFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the lifecycle of all KitsuneCommand features.
 * Replaces the FeatureManager from the C# version.
 */
public class FeatureManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final List<AbstractFeature> features = new ArrayList<>();
    private final SettingsRepository settings;

    // Injected features
    private final PointsFeature pointsFeature;

    @Inject
    public FeatureManager(SettingsRepository settings, PointsFeature pointsFeature) {
        this.settings = settings;
        this.pointsFeature = pointsFeature;
    }

    /**
     * Register all features. Called during the setup phase.
     */
    public void registerAll() {
        register(pointsFeature);
        // Future features will be registered here:
        // register(teleportFeature);
        // register(storeFeature);
        // register(ticketFeature);
        // register(eventVoteFeature);
        // register(taskScheduleFeature);

        LOGGER.info("Registered {} features", features.size());
    }

    /**
     * Register a single feature: load its enabled state and call onEnable if active.
     */
    private void register(AbstractFeature feature) {
        feature.loadEnabledState(settings);
        features.add(feature);

        if (feature.isEnabled()) {
            try {
                feature.onEnable();
                LOGGER.info("  [+] {} enabled", feature.getDisplayName());
            } catch (Exception e) {
                feature.setEnabled(false);
                LOGGER.error("  [!] {} failed to enable: {}", feature.getDisplayName(), e.getMessage());
            }
        } else {
            LOGGER.info("  [-] {} disabled", feature.getDisplayName());
        }
    }

    /**
     * Start all enabled features. Called during the start phase.
     */
    public void startAll() {
        for (AbstractFeature feature : features) {
            if (feature.isEnabled()) {
                try {
                    feature.onStart();
                } catch (Exception e) {
                    LOGGER.error("Feature {} failed to start: {}", feature.getDisplayName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Shutdown all features. Called during the shutdown phase.
     */
    public void shutdownAll() {
        for (AbstractFeature feature : features) {
            if (feature.isEnabled()) {
                try {
                    feature.onShutdown();
                } catch (Exception e) {
                    LOGGER.error("Feature {} failed to shutdown: {}", feature.getDisplayName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Get a feature by its key.
     */
    public Optional<AbstractFeature> getFeature(String key) {
        return features.stream()
            .filter(f -> f.getFeatureKey().equals(key))
            .findFirst();
    }

    /**
     * Get a feature by its class type.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractFeature> Optional<T> getFeature(Class<T> type) {
        return features.stream()
            .filter(type::isInstance)
            .map(f -> (T) f)
            .findFirst();
    }

    public List<AbstractFeature> getAllFeatures() {
        return Collections.unmodifiableList(features);
    }

    public int getFeatureCount() {
        return features.size();
    }

    public long getEnabledCount() {
        return features.stream().filter(AbstractFeature::isEnabled).count();
    }
}
