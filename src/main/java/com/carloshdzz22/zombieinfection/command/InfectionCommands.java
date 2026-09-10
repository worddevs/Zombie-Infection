package com.carloshdzz22.zombieinfection.command;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class InfectionCommands {
	private static final int ADMIN_PERMISSION_LEVEL = 2;

	private InfectionCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerTree(dispatcher));
	}

	private static void registerTree(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("infection")
				.executes(InfectionCommands::query)
				.then(Commands.literal("outbreak").executes(InfectionCommands::outbreak))
				.then(Commands.literal("config")
						.requires(source -> source.hasPermission(ADMIN_PERMISSION_LEVEL))
						.then(Commands.literal("reload").executes(InfectionCommands::reloadConfig))
						.then(Commands.literal("show").executes(InfectionCommands::showConfig)))
				.then(Commands.literal("set")
						.requires(source -> source.hasPermission(ADMIN_PERMISSION_LEVEL))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("level", IntegerArgumentType.integer(
										InfectionManager.MIN_INFECTION, InfectionManager.MAX_INFECTION))
										.executes(InfectionCommands::set))))
				.then(Commands.literal("add")
						.requires(source -> source.hasPermission(ADMIN_PERMISSION_LEVEL))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", IntegerArgumentType.integer(0))
										.executes(InfectionCommands::add))))
				.then(Commands.literal("clear")
						.requires(source -> source.hasPermission(ADMIN_PERMISSION_LEVEL))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(InfectionCommands::clear))));
	}

	private static int query(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		int infection = InfectionManager.getInfection(player);
		context.getSource().sendSuccess(
				() -> Component.translatable("command.zombie-infection.query_status", infection,
						InfectionManager.suppressionRemainingSeconds(player)), false);
		return infection;
	}

	private static int reloadConfig(CommandContext<CommandSourceStack> context) {
		try {
			com.carloshdzz22.zombieinfection.config.GameplayConfig.FILE.reload();
			context.getSource().sendSuccess(() -> Component.translatable("command.zombie-infection.config.reloaded"), true);
			return 1;
		} catch (java.io.IOException | IllegalArgumentException error) {
			context.getSource().sendFailure(Component.translatable("command.zombie-infection.config.failed", error.getMessage()));
			return 0;
		}
	}

	private static int showConfig(CommandContext<CommandSourceStack> context) {
		var config = com.carloshdzz22.zombieinfection.config.GameplayConfig.current();
		context.getSource().sendSuccess(() -> Component.translatable("command.zombie-infection.config.values",
				config.naturalSpawning(), config.densityMultiplier(), config.dayCommonMultiplier(),
				config.dayRunnerMultiplier(), config.daySpecialMultiplier(), config.nightMultiplier(),
				config.minimumPlayerDistance(), config.maximumBlockLight()), false);
		return outbreak(context);
	}

	private static int outbreak(CommandContext<CommandSourceStack> context) {
		var source = context.getSource();
		var level = source.getLevel().getServer().overworld();
		var stage = com.carloshdzz22.zombieinfection.outbreak.OutbreakLevel.from(level);
		var config = com.carloshdzz22.zombieinfection.config.GameplayConfig.current();
		boolean daytime = Math.floorMod(level.getDayTime(), 24000L) < 12000;
		Component multipliers = daytime
				? Component.translatable("command.zombie-infection.day_multipliers", config.dayCommonMultiplier(),
						config.dayRunnerMultiplier(), config.daySpecialMultiplier())
				: Component.literal(Double.toString(config.nightMultiplier()));
		source.sendSuccess(() -> Component.translatable("command.zombie-infection.outbreak",
				com.carloshdzz22.zombieinfection.outbreak.OutbreakLevel.day(level.getDayTime()),
				stage.index(), Component.translatable(stage.nameKey()),
				Component.translatable(daytime
						? "command.zombie-infection.day" : "command.zombie-infection.night"),
				multipliers,
				com.carloshdzz22.zombieinfection.outbreak.OutbreakSpawnRules.effectiveTotalCap(stage),
				com.carloshdzz22.zombieinfection.outbreak.OutbreakSpawnRules.effectiveSpecialCap(stage),
				Component.translatable(config.naturalSpawning() ? "command.zombie-infection.on" : "command.zombie-infection.off")), false);
		return 1;
	}

	private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int value = IntegerArgumentType.getInteger(context, "level");
		int updated = InfectionManager.setInfection(player, value);
		sendAdminFeedback(context.getSource(), "command.zombie-infection.set", player, updated);
		return updated;
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		int updated = InfectionManager.addInfection(player, amount);
		sendAdminFeedback(context.getSource(), "command.zombie-infection.add", player, updated);
		return updated;
	}

	private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		int updated = InfectionManager.setInfection(player, InfectionManager.MIN_INFECTION);
		sendAdminFeedback(context.getSource(), "command.zombie-infection.clear", player, updated);
		return 1;
	}

	private static void sendAdminFeedback(CommandSourceStack source, String translationKey,
			ServerPlayer player, int infection) {
		source.sendSuccess(() -> Component.translatable(translationKey, player.getDisplayName(), infection), true);
		ZombieInfection.LOGGER.info("{} changed {}'s infection to {}%",
				source.getTextName(), player.getGameProfile().getName(), infection);
	}
}
