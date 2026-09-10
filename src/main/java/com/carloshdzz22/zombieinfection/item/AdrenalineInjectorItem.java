package com.carloshdzz22.zombieinfection.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AdrenalineInjectorItem extends TooltipItem {
    public AdrenalineInjectorItem(Properties properties) {
        super(properties, "item.zombie-infection.adrenaline_injector.desc",
                "item.zombie-infection.adrenaline_injector.warning");
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
        var speed = serverPlayer.getEffect(MobEffects.SPEED);
        var strength = serverPlayer.getEffect(MobEffects.STRENGTH);
        if (speed != null && strength != null
                && (speed.isInfiniteDuration() || speed.getDuration() > 20 * 9)
                && (strength.isInfiniteDuration() || strength.getDuration() > 20 * 3)) {
            com.carloshdzz22.zombieinfection.infection.InfectionFeedback.notice(serverPlayer,
                    "message.zombie-infection.adrenaline_active");
            return InteractionResult.SUCCESS_SERVER;
        }
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 45, 0));
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 15, 0));
        serverPlayer.getCooldowns().addCooldown(stack, 20);
        com.carloshdzz22.zombieinfection.infection.InfectionFeedback.treatment(serverPlayer,
                "message.zombie-infection.adrenaline_applied");
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
