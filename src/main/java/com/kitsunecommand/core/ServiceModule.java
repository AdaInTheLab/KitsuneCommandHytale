package com.kitsunecommand.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.kitsunecommand.KitsunePlugin;
import com.kitsunecommand.commands.PointsCommand;
import com.kitsunecommand.commands.SignInCommand;
import com.kitsunecommand.data.DatabaseBootstrap;
import com.kitsunecommand.data.DbConnectionFactory;
import com.kitsunecommand.data.repositories.PointsRepository;
import com.kitsunecommand.data.repositories.SettingsRepository;
import com.kitsunecommand.features.economy.PointsFeature;
import org.jdbi.v3.core.Jdbi;

/**
 * Guice dependency injection module.
 * Replaces the Autofac ServiceRegistry from the C# version.
 */
public class ServiceModule extends AbstractModule {

    private final KitsunePlugin plugin;

    public ServiceModule(KitsunePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        // Bind the plugin instance
        bind(KitsunePlugin.class).toInstance(plugin);

        // Core services
        bind(FeatureManager.class).in(Singleton.class);
        bind(LivePlayerManager.class).in(Singleton.class);

        // Database
        bind(DbConnectionFactory.class).in(Singleton.class);
        bind(DatabaseBootstrap.class).in(Singleton.class);

        // Repositories
        bind(PointsRepository.class).in(Singleton.class);
        bind(SettingsRepository.class).in(Singleton.class);

        // Features
        bind(PointsFeature.class).in(Singleton.class);

        // Commands
        bind(PointsCommand.class).in(Singleton.class);
        bind(SignInCommand.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    Jdbi provideJdbi(DbConnectionFactory connectionFactory) {
        return connectionFactory.getJdbi();
    }
}
