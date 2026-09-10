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

public final class InfectionMedicineItem extends Item {
	private final int reduction;
	private final boolean completeCure;

	public InfectionMedicineItem(Properties properties, int reduction, boolean completeCure) {
		super(properties);
		this.reduction = reduction;
		this.completeCure = completeCure;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context,
			net.minecraft.world.item.component.TooltipDisplay display,
			java.util.function.Consumer<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept((completeCure
				? Component.translatable("item.zombie-infection.cure.desc")
				: Component.translatable("item.zombie-infection.medicine.reduction", reduction))
				.withStyle(net.minecraft.ChatFormatting.GRAY));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		int previous = InfectionManager.getInfection(serverPlayer);
		ItemStack stack = serverPlayer.getItemInHand(hand);
		if (!serverPlayer.isAlive() || serverPlayer.isSpectator() || serverPlayer.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.FAIL;
		}
		if (previous == InfectionManager.MIN_INFECTION) {
			com.carloshdzz22.zombieinfection.infection.InfectionFeedback.notice(serverPlayer,
					"message.zombie-infection.medicine_not_needed");
			return InteractionResult.SUCCESS_SERVER;
		}

		int updated = completeCure
				? InfectionManager.setInfection(serverPlayer, InfectionManager.MIN_INFECTION)
				: InfectionManager.reduceInfection(serverPlayer, reduction);

		serverPlayer.getCooldowns().addCooldown(stack, 20);
		if (!serverPlayer.isCreative()) {
			stack.shrink(1);
		}
		serverPlayer.awardStat(Stats.ITEM_USED.get(this));

		if (completeCure) {
			com.carloshdzz22.zombieinfection.infection.InfectionFeedback.treatment(serverPlayer,
					"message.zombie-infection.cure_applied");
		} else {
			com.carloshdzz22.zombieinfection.infection.InfectionFeedback.treatment(serverPlayer,
					"message.zombie-infection.treatment_reduced", previous - updated, updated);
		}

		return InteractionResult.SUCCESS_SERVER;
	}
}
