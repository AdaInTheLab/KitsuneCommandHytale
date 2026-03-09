package com.kitsunecommand.commands;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.player.Player;
import com.kitsunecommand.data.entities.PointsInfo;
import com.kitsunecommand.data.repositories.PointsRepository;
import com.kitsunecommand.features.economy.PointsFeature;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        this.addAliases(new String[]{"pts", "balance", "bal"});
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        Player player = getPlayer(context);
        String playerId = player.getUuid().toString();

        // Check for "top" subcommand in extra args
        String[] args = context.getRemainingArgs();
        if (args != null && args.length > 0 && "top".equalsIgnoreCase(args[0])) {
            return showLeaderboard(context);
        }

        // Show player's balance
        int balance = pointsFeature.getBalance(playerId);
        context.getSender().sendMessage(
            Message.raw("§6✦ Your Points: §f" + balance)
        );

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> showLeaderboard(CommandContext context) {
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

        context.getSender().sendMessage(Message.raw(sb.toString().trim()));
        return CompletableFuture.completedFuture(null);
    }
}
