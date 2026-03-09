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
import com.kitsunecommand.features.economy.PointsFeature;

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
        this.addAliases("checkin", "daily");
    }

    @Override
    protected void execute(CommandContext context,
                           Store<EntityStore> store,
                           Ref<EntityStore> ref,
                           PlayerRef playerRef,
                           World world) {
        String playerId = playerRef.getUuid().toString();
        int bonus = pointsFeature.processSignIn(playerId);

        if (bonus > 0) {
            // Successful sign-in
            int newBalance = pointsFeature.getBalance(playerId);
            context.sendMessage(
                Message.raw("§a✓ Daily sign-in complete! §6+" + bonus + " points §7(Balance: " + newBalance + ")")
            );
        } else {
            // Already signed in today
            context.sendMessage(
                Message.raw("§cYou've already signed in today! Come back tomorrow.")
            );
        }
    }
}
