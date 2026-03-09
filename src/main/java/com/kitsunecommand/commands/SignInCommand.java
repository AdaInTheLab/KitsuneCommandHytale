package com.kitsunecommand.commands;

import com.google.inject.Inject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.player.Player;
import com.kitsunecommand.features.economy.PointsFeature;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /signin command — claims daily sign-in bonus points.
 *
 * Usage:
 *   /signin — claim your daily bonus
 */
public class SignInCommand extends AbstractPlayerCommand {

    private final PointsFeature pointsFeature;

    @Inject
    public SignInCommand(PointsFeature pointsFeature) {
        super("signin", "kitsunecommand.commands.signin.desc");
        this.pointsFeature = pointsFeature;
        this.addAliases(new String[]{"checkin", "daily"});
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        Player player = getPlayer(context);
        String playerId = player.getUuid().toString();

        int bonus = pointsFeature.processSignIn(playerId);

        if (bonus > 0) {
            // Successful sign-in
            int newBalance = pointsFeature.getBalance(playerId);
            context.getSender().sendMessage(
                Message.raw("§a✓ Daily sign-in complete! §6+" + bonus + " points §7(Balance: " + newBalance + ")")
            );
        } else {
            // Already signed in today
            context.getSender().sendMessage(
                Message.raw("§cYou've already signed in today! Come back tomorrow.")
            );
        }

        return CompletableFuture.completedFuture(null);
    }
}
