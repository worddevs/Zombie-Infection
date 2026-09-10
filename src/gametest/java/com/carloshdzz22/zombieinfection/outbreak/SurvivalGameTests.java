package com.carloshdzz22.zombieinfection.outbreak;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.config.*;
import com.carloshdzz22.zombieinfection.entity.InfectedCloudEntity;
import com.carloshdzz22.zombieinfection.entity.projectile.InfectedSpitEntity;
import com.carloshdzz22.zombieinfection.infection.*;
import com.carloshdzz22.zombieinfection.networking.InfectionAttachments;
import com.carloshdzz22.zombieinfection.registry.*;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import java.nio.file.Files;
import java.util.*;

/** Only dev.5 survival/config/medicine/notification/light/loot checks; no historical suite. */
public class SurvivalGameTests {
    @GameTest(maxTicks = 220, skyAccess = true)
    public void survivalPolish(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        byte[] original = Files.readAllBytes(GameplayConfig.FILE.path());
        StringBuilder overrides = new StringBuilder("densityMultiplier=2\ndayCommonMultiplier=1\ndayRunnerMultiplier=1\ndaySpecialMultiplier=1\n");
        int amount = 1;
        for (var source : InfectionSource.values()) {
            overrides.append(source.prefix()).append("chance=1\n").append(source.prefix()).append("min=").append(amount)
                    .append('\n').append(source.prefix()).append("max=").append(amount++).append('\n');
        }
        Files.writeString(GameplayConfig.FILE.path(), overrides);
        GameplayConfig.FILE.reload();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        level.setDayTime(6000);
        // A real enclosed room with a torch, plus a separate outdoor spawn site.
        for (int x = 2; x <= 6; x++) for (int z = 2; z <= 6; z++) for (int y = 1; y <= 5; y++) {
            boolean boundary = x == 2 || x == 6 || z == 2 || z == 6 || y == 1 || y == 5;
            h.setBlock(new BlockPos(x, y, z), boundary ? Blocks.STONE : Blocks.AIR);
        }
        h.setBlock(new BlockPos(3, 2, 4), Blocks.TORCH);
        BlockPos room = h.absolutePos(new BlockPos(4, 2, 4));
        BlockPos outside = h.absolutePos(new BlockPos(11, 2, 4));
        h.setBlock(new BlockPos(11, 1, 4), Blocks.STONE);
        ServerPlayer[] players = new ServerPlayer[2];
        h.runAfterDelay(40, () -> {
            check(h, level.getBrightness(LightLayer.BLOCK, room) > 7 && level.getBrightness(LightLayer.SKY, room) == 0,
                    "Indoor fixture lighting: block=" + level.getBrightness(LightLayer.BLOCK, room)
                            + ", sky=" + level.getBrightness(LightLayer.SKY, room)
                            + ", torch=" + level.getBlockState(h.absolutePos(new BlockPos(3, 2, 4)))
                            + ", roof=" + level.getBlockState(room.above(3)));
            check(h, level.getBrightness(LightLayer.BLOCK, outside) <= 7, "Torch does not protect the outdoor site at a distance");
            for (long time : new long[]{6000, 18000}) {
                level.setDayTime(time);
                for (var type : List.of(ModEntities.INFECTED, ModEntities.RUNNER, ModEntities.BLOATER, ModEntities.SPITTER)) {
                    RandomSource zero = new LegacyRandomSource(1) { @Override public double nextDouble() { return 0; } };
                    check(h, !OutbreakSpawnRules.checkInfectedSpawn(type, level, EntitySpawnReason.NATURAL, room, zero),
                            "Artificial light blocks " + type + " at " + time);
                    check(h, OutbreakSpawnRules.checkInfectedSpawn(type, level, EntitySpawnReason.NATURAL, outside, zero),
                            "Outdoor day/night spawning still accepts " + type);
                }
            }
            Vec3 position = Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2, 9, 2)));
            players[0] = player(h, "SurvivalA", position);
            players[1] = player(h, "SurvivalB", position.add(8, 0, 0));
            verifyStateAndMedicines(h, players[0], players[1]);
            verifyNotifications(h);
            verifyLoot(h, position);
        });
        // Let the vanilla join protection expire before testing real melee and projectile damage.
        h.runAfterDelay(150, () -> {
            try {
                var a = players[0];
                InfectionManager.applySuppression(a, 0);
                a.setHealth(100);
                var types = List.of(ModEntities.INFECTED, ModEntities.RUNNER, ModEntities.BLOATER, ModEntities.SPITTER);
                for (int index = 0; index < types.size(); index++) {
                    var mob = h.spawn(types.get(index), new BlockPos(1, 9, 1));
                    mob.setNoAi(true);
                    a.invulnerableTime = 0;
                    InfectionManager.setInfection(a, 0);
                    check(h, mob.doHurtTarget(level, a), "Real melee damage accepted for " + types.get(index));
                    check(h, InfectionManager.getInfection(a) == index + 1, "Melee uses its own live profile exactly once");
                    mob.discard();
                }
                InfectionManager.setInfection(a, 0);
                a.invulnerableTime = 0;
                var spitter = h.spawn(ModEntities.SPITTER, new BlockPos(1, 9, 1));
                spitter.setNoAi(true);
                var spit = new InfectedSpitEntity(level, spitter);
                spit.setPos(a.position());
                var onHit = InfectedSpitEntity.class.getDeclaredMethod("onHit", HitResult.class);
                onHit.setAccessible(true);
                onHit.invoke(spit, new EntityHitResult(a));
                check(h, InfectionManager.getInfection(a) == 5, "Spitter projectile uses live projectile profile");
                var clouds = level.getEntitiesOfClass(InfectedCloudEntity.class, a.getBoundingBox().inflate(5));
                check(h, clouds.stream().anyMatch(c -> c.infectionSource() == InfectionSource.SPITTER_ZONE),
                        "Spit impact creates the zone category");
                clouds.forEach(Entity::discard);
                spitter.discard();
                verifyCloudPersistence(h, a);

                // Changing config affects an already-created source and cannot partially apply invalid input.
                Files.writeString(GameplayConfig.FILE.path(), "infection.common.chance=0\n");
                GameplayConfig.FILE.reload();
                InfectionManager.setInfection(a, 0);
                check(h, !InfectionManager.tryInfect(a, a.getRandom(), InfectionSource.COMMON), "Reload disables infection live");
                var valid = GameplayConfig.current();
                Files.writeString(GameplayConfig.FILE.path(), "infection.common.chance=NaN\n");
                try { GameplayConfig.FILE.reload(); throw new AssertionError("Invalid reload accepted"); }
                catch (IllegalArgumentException expected) { }
                check(h, GameplayConfig.current() == valid, "Invalid server reload retains previous complete settings");
                var source = level.getServer().createCommandSourceStack().withEntity(a).withPermission(0);
                var dispatcher = level.getServer().getCommands().getDispatcher();
                dispatcher.execute("infection", source);
                check(h, dispatcher.execute("infection outbreak", source) == 1, "Public compact status commands execute");
                h.succeed();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                try { Files.write(GameplayConfig.FILE.path(), original); GameplayConfig.FILE.reload(); }
                catch (Exception exception) { throw new RuntimeException(exception); }
                for (var player : players) if (player != null) player.discard();
            }
        });
    }

    @GameTest(maxTicks = 20)
    public void outbreakRestart(GameTestHelper h) {
        // Run separately against the same isolated server directory after survivalPolish shuts down.
        if (!Boolean.getBoolean("zombie-infection.verifyOutbreakRestart")) {
            h.succeed(); // Opt-in because an ordinary fresh GameTest world has no prior restart fixture.
            return;
        }
        var level = h.getLevel();
        check(h, level.getAttachedOrElse(OutbreakNotifications.HIGHEST_ANNOUNCED, -1) == 4,
                "Highest announced level survived an actual server restart");
        OutbreakNotifications.baseline(level);
        check(h, !OutbreakNotifications.recordIncrease(level, 4), "Restart does not replay the already-announced level");
        h.succeed();
    }

    private static void verifyStateAndMedicines(GameTestHelper h, ServerPlayer a, ServerPlayer b) {
        check(h, !a.isCreative() && !b.isCreative(), "Use real survival players");
        check(h, InfectionFeedback.notice(b, "message.zombie-infection.contagion_blocked")
                && !InfectionFeedback.notice(b, "message.zombie-infection.contagion_blocked"), "Notice throttle blocks repeated spam");
        check(h, InfectionFeedback.notice(a, "message.zombie-infection.contagion_blocked"), "Feedback throttle is per player");
        int value = 1;
        for (var source : InfectionSource.values()) {
            InfectionManager.setInfection(a, 0);
            check(h, InfectionManager.tryInfect(a, a.getRandom(), source) && InfectionManager.getInfection(a) == value++,
                    "Config routes every infection source " + source);
        }
        check(h, InfectionManager.getInfection(b) == 0, "Infection never leaks to another player");
        InfectionManager.setInfection(a, 0);
        for (Item item : List.of(ModItems.BASIC_ANTIVIRAL, ModItems.ANTIVIRAL_INJECTION, ModItems.CURE)) {
            ItemStack stack = equip(a, item, 2); item.use(a.level(), a, InteractionHand.MAIN_HAND);
            check(h, stack.getCount() == 2 && InfectionManager.getInfection(a) == 0, "No medicine consumed at zero infection");
            InfectionManager.setInfection(a, 5);
            item.use(a.level(), a, InteractionHand.MAIN_HAND);
            check(h, stack.getCount() == 1 && InfectionManager.getInfection(a) == 0, "Treatment clamps reduction at zero");
            a.getCooldowns().removeCooldown(a.getCooldowns().getCooldownGroup(stack));
        }
        InfectionManager.setInfection(a, 45);
        var suppression = equip(a, ModItems.INFECTION_SUPPRESSANT, 3);
        suppression.getItem().use(a.level(), a, InteractionHand.MAIN_HAND);
        long until = a.getAttachedOrElse(InfectionAttachments.SUPPRESSION_UNTIL, 0L);
        check(h, suppression.getCount() == 2 && InfectionManager.suppressionRemainingSeconds(a) == 180,
                "Suppressant starts three minutes and consumes one");
        check(h, InfectionManager.getInfection(a) == 45 && !InfectionManager.tryInfect(a, a.getRandom(), InfectionSource.RUNNER),
                "Suppressant blocks new infection but does not cure existing infection");
        check(h, InfectionManager.tryInfect(b, b.getRandom(), InfectionSource.RUNNER), "Suppression is not shared with the other player");
        a.getCooldowns().removeCooldown(a.getCooldowns().getCooldownGroup(suppression));
        suppression.getItem().use(a.level(), a, InteractionHand.MAIN_HAND);
        check(h, suppression.getCount() == 2 && a.getAttachedOrElse(InfectionAttachments.SUPPRESSION_UNTIL, 0L) == until,
                "Early renewal is rejected without consuming or extending duration");
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, a.level().registryAccess());
        a.saveWithoutId(saved);
        check(h, saved.buildResult().toString().contains("zombie-infection:suppression_until"), "Suppression expiry is included in saved player data");
        a.setAttached(InfectionAttachments.SUPPRESSION_UNTIL, InfectionManager.serverTime(a) + 600);
        suppression.getItem().use(a.level(), a, InteractionHand.MAIN_HAND);
        check(h, suppression.getCount() == 1 && InfectionManager.suppressionRemainingTicks(a) == 3600,
                "Late renewal resets to three minutes without stacking time");
        InfectionManager.applySuppression(a, 0);
        check(h, !InfectionManager.isSuppressed(a), "Expired suppressant stops blocking infection");
        var adrenaline = equip(a, ModItems.ADRENALINE_INJECTOR, 2);
        adrenaline.getItem().use(a.level(), a, InteractionHand.MAIN_HAND);
        a.getCooldowns().removeCooldown(a.getCooldowns().getCooldownGroup(adrenaline));
        adrenaline.getItem().use(a.level(), a, InteractionHand.MAIN_HAND);
        check(h, adrenaline.getCount() == 1, "Fresh adrenaline cannot be wasted by repeated use");
        InfectionManager.setInfection(a, 0);
        check(h, !InfectionManager.tryInfect(a, a.getRandom(), 1, Integer.MIN_VALUE, 0, "bounds")
                && InfectionManager.getInfection(a) == 0, "Invalid negative ranges cannot overflow or fake a contagion");
        check(h, !InfectionManager.tryInfect(a, a.getRandom(), Float.NaN, 1, 2, "nan"), "Nonfinite direct infection chance rejected");
    }

    private static void verifyNotifications(GameTestHelper h) {
        var level = h.getLevel();
        level.setAttached(OutbreakNotifications.HIGHEST_ANNOUNCED, 0);
        for (int index = 1; index <= 4; index++) {
            check(h, OutbreakNotifications.recordIncrease(level, index), "First level crossing announces " + index);
            OutbreakNotifications.baseline(level);
            check(h, !OutbreakNotifications.recordIncrease(level, index), "Repeated tick/login initialization cannot repeat " + index);
        }
        check(h, !OutbreakNotifications.recordIncrease(level, 1) && !OutbreakNotifications.recordIncrease(level, 4),
                "Moving time backward and forward cannot replay notifications");
    }

    private static void verifyCloudPersistence(GameTestHelper h, ServerPlayer player) {
        for (var source : List.of(InfectionSource.SPITTER_ZONE, InfectionSource.BLOATER_CLOUD)) {
            var cloud = new InfectedCloudEntity(ModEntities.INFECTED_CLOUD, h.getLevel());
            cloud.configure(null, source == InfectionSource.SPITTER_ZONE ? 2.25F : 3.5F, 100, source);
            cloud.setPos(player.position());
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
            cloud.saveWithoutId(output);
            var restored = new InfectedCloudEntity(ModEntities.INFECTED_CLOUD, h.getLevel());
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
            check(h, restored.infectionSource() == source, "Cloud save/load preserves category " + source);
            InfectionManager.setInfection(player, 0);
            for (int tick = 0; tick < 20; tick++) {
                // The level normally increments this counter before invoking an entity's tick.
                restored.tickCount++;
                restored.tick();
            }
            check(h, InfectionManager.getInfection(player) == (source == InfectionSource.SPITTER_ZONE ? 6 : 7),
                    "Restored cloud uses its live config profile");
            restored.discard(); cloud.discard();
        }
    }

    private static void verifyLoot(GameTestHelper h, Vec3 origin) {
        var level = h.getLevel();
        for (String name : List.of("abandoned_clinic", "abandoned_pharmacy")) {
            var key = ResourceKey.create(Registries.LOOT_TABLE, ZombieInfection.id("chests/" + name));
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            String json = LootTable.DIRECT_CODEC.encodeStart(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), table)
                    .getOrThrow().toString();
            check(h, !json.contains("zombie-infection:cure") && json.contains("zombie-infection:antiviral_injection"),
                    "Clinic/pharmacy table excludes Cure but retains rare injection");
            var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin).create(LootContextParamSets.CHEST);
            for (long seed = 1; seed <= 512; seed++) {
                int medication = 0;
                for (ItemStack stack : table.getRandomItems(params, seed)) {
                    check(h, !stack.is(ModItems.CURE), "Cure never generated in normal clinic/pharmacy loot");
                    if (stack.is(ModItems.BASIC_ANTIVIRAL) || stack.is(ModItems.ANTIVIRAL_INJECTION)) medication += stack.getCount();
                }
                check(h, medication <= 1, "At most one antiviral item per chest");
            }
        }
    }

    private static ItemStack equip(ServerPlayer player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count); player.getInventory().setSelectedItem(stack); return stack;
    }

    private static ServerPlayer player(GameTestHelper h, String name, Vec3 position) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND); new EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL); player.setPos(position); player.setNoGravity(true);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); player.setHealth(100);
        return player;
    }

    private static void check(GameTestHelper h, boolean value, String message) { h.assertTrue(value, Component.literal(message)); }
}
