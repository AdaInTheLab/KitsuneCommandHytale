package com.kitsunecommand.data;

import com.google.inject.Inject;
import com.hypixel.hytale.logger.HytaleLogger;
import org.jdbi.v3.core.Jdbi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles database initialization and schema migrations.
 * Equivalent to DatabaseBootstrap.cs in the C# version.
 *
 * Migrations are embedded SQL files in resources/migrations/ and run in order.
 * A schema_version table tracks which migrations have already been applied.
 */
public class DatabaseBootstrap {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String[] MIGRATIONS = {
        "001_initial_schema.sql",
        "002_user_accounts.sql",
        "003_purchase_history.sql",
        "004_vip_gift_enhancements.sql",
        "005_task_schedule_interval.sql",
        "006_backups.sql",
        "007_tickets.sql",
        "008_player_metadata.sql"
    };

    private final DbConnectionFactory connectionFactory;
    private final Jdbi jdbi;

    @Inject
    public DatabaseBootstrap(DbConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.jdbi = connectionFactory.getJdbi();
    }

    /**
     * Initialize the database: create the version tracking table and run any pending migrations.
     */
    public void initialize() {
        LOGGER.at(Level.INFO).log("Running database migrations...");

        // Create the migration tracking table
        jdbi.useHandle(handle -> {
            handle.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER PRIMARY KEY,
                    filename TEXT NOT NULL,
                    applied_at TEXT NOT NULL DEFAULT (datetime('now'))
                )
            """);
        });

        // Run pending migrations
        int applied = 0;
        for (int i = 0; i < MIGRATIONS.length; i++) {
            final int version = i + 1;
            final String filename = MIGRATIONS[i];

            boolean alreadyApplied = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM schema_version WHERE version = :v")
                    .bind("v", version)
                    .mapTo(Integer.class)
                    .one() > 0
            );

            if (!alreadyApplied) {
                String sql = loadMigration(filename);
                if (sql != null) {
                    jdbi.useHandle(handle -> {
                        // Split on semicolons and execute each statement
                        for (String statement : sql.split(";")) {
                            String trimmed = statement.trim();
                            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                                handle.execute(trimmed);
                            }
                        }
                        // Record the migration
                        handle.execute(
                            "INSERT INTO schema_version (version, filename) VALUES (?, ?)",
                            version, filename
                        );
                    });
                    LOGGER.at(Level.INFO).log("  Applied migration %d: %s", version, filename);
                    applied++;
                } else {
                    LOGGER.at(Level.WARNING).log("  Migration file not found: %s", filename);
                }
            }
        }

        if (applied > 0) {
            LOGGER.at(Level.INFO).log("Database migrations complete — %d new migrations applied.", applied);
        } else {
            LOGGER.at(Level.INFO).log("Database is up to date — no new migrations.");
        }
    }

    /**
     * Load a migration SQL file from classpath resources.
     */
    private String loadMigration(String filename) {
        String path = "migrations/" + filename;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).withCause(e).log("Failed to load migration %s", filename);
            return null;
        }
    }

    /**
     * Close the database connection pool.
     */
    public void close() {
        connectionFactory.close();
    }
}
