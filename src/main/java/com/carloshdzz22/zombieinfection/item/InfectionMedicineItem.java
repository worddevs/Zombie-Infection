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
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		int previous = InfectionManager.getInfection(serverPlayer);
		if (previous == InfectionManager.MIN_INFECTION) {
			serverPlayer.sendSystemMessage(Component.translatable("message.zombie-infection.medicine_not_needed"));
			return InteractionResult.SUCCESS_SERVER;
		}

		int updated = completeCure
				? InfectionManager.setInfection(serverPlayer, InfectionManager.MIN_INFECTION)
				: InfectionManager.reduceInfection(serverPlayer, reduction);

		ItemStack stack = serverPlayer.getItemInHand(hand);
		if (!serverPlayer.isCreative()) {
			stack.shrink(1);
		}
		serverPlayer.awardStat(Stats.ITEM_USED.get(this));

		if (updated == InfectionManager.MIN_INFECTION) {
			serverPlayer.sendSystemMessage(Component.translatable("message.zombie-infection.infection_cleared"));
		} else {
			serverPlayer.sendSystemMessage(Component.translatable(
					"message.zombie-infection.infection_reduced", previous - updated, updated));
		}

		return InteractionResult.SUCCESS_SERVER;
	}
}
