package com.carloshdzz22.zombieinfection.item;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class InfectionSuppressantItem extends TooltipItem {
    private static final int DURATION_TICKS = 20 * 60 * 3;

    public InfectionSuppressantItem(Properties properties) {
        super(properties, "item.zombie-infection.infection_suppressant.desc",
                "item.zombie-infection.infection_suppressant.warning", "item.zombie-infection.infection_suppressant.status");
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = serverPlayer.getItemInHand(hand);
        if (!serverPlayer.isAlive() || serverPlayer.isSpectator() || serverPlayer.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (InfectionManager.suppressionRemainingTicks(serverPlayer) > 20 * 30) {
            com.carloshdzz22.zombieinfection.infection.InfectionFeedback.notice(serverPlayer,
                    "message.zombie-infection.suppression_active", InfectionManager.suppressionRemainingSeconds(serverPlayer));
            return InteractionResult.SUCCESS_SERVER;
        }
        InfectionManager.applySuppression(serverPlayer, DURATION_TICKS);
        com.carloshdzz22.zombieinfection.infection.InfectionFeedback.treatment(serverPlayer,
                "message.zombie-infection.suppression_applied", DURATION_TICKS / 20);
        serverPlayer.getCooldowns().addCooldown(stack, 20);
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
