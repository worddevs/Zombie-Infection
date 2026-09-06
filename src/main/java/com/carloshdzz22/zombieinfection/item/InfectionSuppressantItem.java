package com.carloshdzz22.zombieinfection.item;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class InfectionSuppressantItem extends Item {
    private static final int DURATION_TICKS = 20 * 60 * 3;

    public InfectionSuppressantItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        InfectionManager.applySuppression(serverPlayer, DURATION_TICKS);
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.zombie-infection.suppression_applied", DURATION_TICKS / 20));
        ItemStack stack = serverPlayer.getItemInHand(hand);
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
