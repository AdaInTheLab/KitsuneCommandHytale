package com.kitsunecommand;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hypixel.hytale.server.core.logging.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.kitsunecommand.commands.PointsCommand;
import com.kitsunecommand.commands.SignInCommand;
import com.kitsunecommand.core.FeatureManager;
import com.kitsunecommand.core.LivePlayerManager;
import com.kitsunecommand.core.ServiceModule;
import com.kitsunecommand.data.DatabaseBootstrap;

import javax.annotation.Nonnull;

/**
 * KitsuneCommand — Hytale Edition
 * Server management and economy plugin.
 */
public class KitsunePlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static KitsunePlugin instance;

    private Injector injector;
    private DatabaseBootstrap database;
    private FeatureManager featureManager;
    private LivePlayerManager livePlayerManager;

    public KitsunePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        LOGGER.info("=== KitsuneCommand Hytale Edition v1.0.0 ===");
        LOGGER.info("Setting up...");

        // Initialize Guice DI
        injector = Guice.createInjector(new ServiceModule(this));

        // Initialize database and run migrations
        database = injector.getInstance(DatabaseBootstrap.class);
        database.initialize();

        // Initialize feature manager and register features
        featureManager = injector.getInstance(FeatureManager.class);
        featureManager.registerAll();

        // Initialize live player tracking
        livePlayerManager = injector.getInstance(LivePlayerManager.class);
        livePlayerManager.register(getEventRegistry());

        // Register commands
        getCommandRegistry().registerCommand(injector.getInstance(PointsCommand.class));
        getCommandRegistry().registerCommand(injector.getInstance(SignInCommand.class));

        LOGGER.info("Setup complete — {} features registered", featureManager.getFeatureCount());
    }

    @Override
    protected void start() {
        // Start all features (safe to interact with other plugins now)
        featureManager.startAll();

        LOGGER.info("KitsuneCommand is now running!");
        LOGGER.info("  Data directory: {}", getDataDirectory());
        LOGGER.info("  Features active: {}", featureManager.getEnabledCount());
    }

    @Override
    protected void shutdown() {
        LOGGER.info("KitsuneCommand shutting down...");

        // Shutdown features
        if (featureManager != null) {
            featureManager.shutdownAll();
        }

        // Close database connections
        if (database != null) {
            database.close();
        }

        instance = null;
        LOGGER.info("KitsuneCommand shutdown complete.");
    }

    // --- Public API ---

    public static KitsunePlugin getInstance() {
        return instance;
    }

    public Injector getInjector() {
        return injector;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public LivePlayerManager getLivePlayerManager() {
        return livePlayerManager;
    }
}
