package com.kitsunecommand.data;

import com.google.inject.Inject;
import com.hypixel.hytale.logger.HytaleLogger;
import com.kitsunecommand.KitsunePlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Manages the HikariCP connection pool and JDBI instance for SQLite.
 * Equivalent to the Dapper-based connection management in the C# version.
 */
public class DbConnectionFactory {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final HikariDataSource dataSource;
    private final Jdbi jdbi;

    @Inject
    public DbConnectionFactory(KitsunePlugin plugin) {
        Path dbPath = plugin.getDataDirectory().resolve("kitsunecommand.db");
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();

        LOGGER.at(Level.INFO).log("Initializing SQLite database at: %s", dbPath);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("KitsuneCommand-SQLite");

        // SQLite-specific: enforce WAL mode and foreign keys
        config.addDataSourceProperty("journal_mode", "WAL");
        config.setConnectionInitSql("PRAGMA foreign_keys = ON; PRAGMA journal_mode = WAL;");

        this.dataSource = new HikariDataSource(config);
        this.jdbi = Jdbi.create(dataSource);

        LOGGER.at(Level.INFO).log("Database connection pool initialized (max=%d, pool=%s)",
            config.getMaximumPoolSize(), config.getPoolName());
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.at(Level.INFO).log("Database connection pool closed.");
        }
    }
}
