package com.carloshdzz22.zombieinfection.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AdrenalineInjectorItem extends Item {
    public AdrenalineInjectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 45, 0));
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 15, 0));
        ItemStack stack = serverPlayer.getItemInHand(hand);
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
