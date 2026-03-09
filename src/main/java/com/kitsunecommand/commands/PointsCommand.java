package com.kitsunecommand.commands;

import com.google.inject.Inject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kitsunecommand.data.entities.PointsInfo;
import com.kitsunecommand.data.repositories.PointsRepository;
import com.kitsunecommand.features.economy.PointsFeature;

import java.util.List;

/**
 * /points command — shows a player's points balance and leaderboard.
 *
 * Usage:
 *   /points        — show your balance
 *   /points top    — show top 10 leaderboard
 */
public class PointsCommand extends AbstractPlayerCommand {

    private final PointsFeature pointsFeature;
    private final PointsRepository pointsRepo;

    @Inject
    public PointsCommand(PointsFeature pointsFeature, PointsRepository pointsRepo) {
        super("points", "kitsunecommand.commands.points.desc");
        this.pointsFeature = pointsFeature;
        this.pointsRepo = pointsRepo;
        this.addAliases("pts", "balance", "bal");
        this.setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(CommandContext context,
                           Store<EntityStore> store,
                           Ref<EntityStore> ref,
                           PlayerRef playerRef,
                           World world) {
        // Check for "top" subcommand in raw input
        String input = context.getInputString();
        if (input != null && input.toLowerCase().contains("top")) {
            showLeaderboard(context);
            return;
        }

        // Show player's balance
        String playerId = playerRef.getUuid().toString();
        int balance = pointsFeature.getBalance(playerId);
        context.sendMessage(Message.raw("§6✦ Your Points: §f" + balance));
    }

    private void showLeaderboard(CommandContext context) {
        List<PointsInfo> top = pointsRepo.getLeaderboard(10);

        StringBuilder sb = new StringBuilder();
        sb.append("§6✦ Points Leaderboard §7(Top 10)\n");
        sb.append("§7─────────────────────\n");

        for (int i = 0; i < top.size(); i++) {
            PointsInfo info = top.get(i);
            String medal = switch (i) {
                case 0 -> "§e🥇";
                case 1 -> "§f🥈";
                case 2 -> "§c🥉";
                default -> "§7 " + (i + 1) + ".";
            };
            sb.append(medal).append(" §f")
                .append(info.getPlayerName() != null ? info.getPlayerName() : "Unknown")
                .append(" §7— §6").append(info.getPoints())
                .append(" pts\n");
        }

        context.sendMessage(Message.raw(sb.toString().trim()));
    }
}
