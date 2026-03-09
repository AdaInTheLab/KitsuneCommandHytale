# KitsuneCommand — Hytale Edition

Server management and economy plugin for Hytale. Points, teleport, shop, tickets, and a web admin panel — reimagined from the [7 Days to Die version](https://github.com/AdaInTheLab/KitsuneCommand) for Hytale's native plugin API.

> **Status:** Phase 1 — economy foundation compiles and runs. More features coming.

## What It Does

**Right now (Phase 1):**
- Points economy with kill tracking, playtime rewards, and daily sign-in bonuses
- `/points` — check your balance
- `/points top` — leaderboard (top 10)
- `/signin` (or `/checkin`, `/daily`) — claim daily bonus
- SQLite database with automatic schema migrations
- All settings stored in DB and hot-configurable

**Coming next:**
- `/home`, `/tp` — saved locations and teleportation
- `/shop` — in-game store (buy items with points)
- `/ticket` — support ticket system
- `/voteskip` — generic event voting (skip night, trigger boss, etc.)
- Web admin panel (Javalin + Vue 3)
- In-game Custom UI pages
- Discord webhook integration

## Requirements

- Hytale Dedicated Server
- Java 25+
- Gradle 9.4+ (included via wrapper)

## Build

```bash
./gradlew shadowJar
```

Output: `build/libs/KitsuneCommand-1.0.0.jar`

## Install

Drop the JAR into your server's `mods/` directory:

```bash
./gradlew deployToServer   # copies to server/mods/
```

Or manually:

```bash
cp build/libs/KitsuneCommand-1.0.0.jar /path/to/server/mods/
```

## Project Structure

```
src/main/java/com/kitsunecommand/
├── KitsunePlugin.java              # Entry point (extends JavaPlugin)
├── KitsuneAPI.java                 # Public API for other plugins
├── config/
│   └── KitsuneConfig.java          # Plugin configuration
├── core/
│   ├── ServiceModule.java          # Guice DI bindings
│   ├── FeatureManager.java         # Feature lifecycle management
│   ├── AbstractFeature.java        # Base class for all features
│   └── LivePlayerManager.java      # Online player tracking
├── commands/
│   ├── PointsCommand.java          # /points, /points top
│   └── SignInCommand.java          # /signin, /checkin, /daily
├── data/
│   ├── DatabaseBootstrap.java      # SQLite init + migrations
│   ├── DbConnectionFactory.java    # HikariCP connection pool
│   ├── entities/
│   │   └── PointsInfo.java         # Points record POJO
│   └── repositories/
│       ├── PointsRepository.java   # Points CRUD
│       └── SettingsRepository.java # Key-value settings
├── features/
│   └── economy/
│       ├── PointsFeature.java      # Kill/playtime/signin rewards
│       └── PointsSettings.java     # Economy configuration
└── services/                       # (Phase 3+)

src/main/resources/
├── manifest.json                   # Hytale plugin manifest
└── migrations/                     # SQLite schema (001-008)
```

## Tech Stack

| Layer | Library | Why |
|-------|---------|-----|
| DI | Guice 7 | Manages 15+ injectables cleanly |
| Database | SQLite + HikariCP + JDBI 3 | Lightweight, zero-config, SQL mapping |
| Web (Phase 3) | Javalin 6 | Embedded HTTP + WebSocket |
| Auth (Phase 3) | java-jwt | JWT tokens for the web panel |
| JSON | Gson | Standard Java JSON |

All dependencies are relocated in the shadow JAR to avoid classpath conflicts with other plugins.

## Configuration

Feature settings live in the SQLite database (`kitsunecommand.db`) and can be changed at runtime. Defaults:

| Setting | Default | Description |
|---------|---------|-------------|
| `points.killReward` | 10 | Points per kill |
| `points.signInBonus` | 50 | Daily sign-in reward |
| `points.playtimeReward` | 5 | Points per playtime interval |
| `points.playtimeIntervalMinutes` | 30 | Minutes between playtime rewards |
| `points.killTrackingEnabled` | true | Award points for kills |
| `points.playtimeTrackingEnabled` | true | Award points for being online |
| `points.signInEnabled` | true | Allow daily sign-in bonus |

## For Other Plugin Developers

KitsuneCommand exposes a public API:

```java
KitsuneAPI api = KitsuneAPI.get();

// Check a player's balance
int balance = api.getPoints().getBalance(playerUuid);

// Access the points repository directly
api.getPointsRepository().addPoints(playerUuid, 100);

// Check who's online
api.getLivePlayerManager().isOnline(playerUuid);
```

## Phased Roadmap

1. **Phase 1** ✅ — Economy foundation (points, sign-in, commands, DB)
2. **Phase 2** — Commands & features (homes, teleport, shop, tickets, voting)
3. **Phase 3** — Web admin panel (Javalin + Vue 3, JWT auth, WebSocket)
4. **Phase 4** — In-game Custom UI, Discord webhooks, localization
5. **Phase 5** — Public API polish, docs, release

## License

MIT

---

Built by [Ada](https://github.com/AdaInTheLab) with help from Claude.
