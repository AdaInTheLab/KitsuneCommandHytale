package com.kitsunecommand;

import com.kitsunecommand.core.FeatureManager;
import com.kitsunecommand.core.LivePlayerManager;
import com.kitsunecommand.data.repositories.PointsRepository;
import com.kitsunecommand.features.economy.PointsFeature;

/**
 * Public API interface for third-party plugins.
 * Other plugins can depend on KitsuneCommand and access these services.
 *
 * Usage from another plugin:
 *   KitsuneAPI api = KitsuneAPI.get();
 *   int balance = api.getPoints().getBalance(playerId);
 */
public class KitsuneAPI {

    private static KitsuneAPI instance;

    private final KitsunePlugin plugin;

    private KitsuneAPI(KitsunePlugin plugin) {
        this.plugin = plugin;
    }

    public static void init(KitsunePlugin plugin) {
        instance = new KitsuneAPI(plugin);
    }

    public static KitsuneAPI get() {
        if (instance == null) {
            throw new IllegalStateException("KitsuneCommand is not initialized yet");
        }
        return instance;
    }

    // --- Feature Access ---

    public PointsFeature getPoints() {
        return plugin.getFeatureManager()
            .getFeature(PointsFeature.class)
            .orElseThrow(() -> new IllegalStateException("Points feature is not available"));
    }

    public FeatureManager getFeatureManager() {
        return plugin.getFeatureManager();
    }

    public LivePlayerManager getLivePlayerManager() {
        return plugin.getLivePlayerManager();
    }

    public PointsRepository getPointsRepository() {
        return plugin.getInjector().getInstance(PointsRepository.class);
    }
}
