package com.carloshdzz22.zombieinfection.outbreak;

import com.carloshdzz22.zombieinfection.config.VanillaHostileSpawnConfig;
import com.carloshdzz22.zombieinfection.entity.CommonInfectedEntity;
import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import com.carloshdzz22.zombieinfection.perception.InfectedHearing;
import com.carloshdzz22.zombieinfection.perception.NoiseManager;
import com.carloshdzz22.zombieinfection.perception.NoiseType;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.carloshdzz22.zombieinfection.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** One isolated fixture avoids cross-test interference with population and hearing AABBs. */
public class InfectedGameTests {
    @GameTest(maxTicks = 80, skyAccess = true)
    public void configurationControls(GameTestHelper helper) throws Exception {
        var file = com.carloshdzz22.zombieinfection.config.GameplayConfig.FILE;
        byte[] original = java.nio.file.Files.readAllBytes(file.path());
        var level = helper.getLevel();
        var source = level.getServer().createCommandSourceStack().withLevel(level);
        var dispatcher = level.getServer().getCommands().getDispatcher();
        try {
            var root = dispatcher.getRoot().getChild("infection");
            require(helper, !root.getChild("config").canUse(source.withPermission(0))
                    && root.getChild("config").canUse(source.withPermission(2)), "Config commands require operator level 2");
            require(helper, root.getChild("outbreak").canUse(source.withPermission(0)), "Outbreak query is available to players");
            java.nio.file.Files.writeString(file.path(), "naturalSpawning=false\ntotalCapMultiplier=0.5\nspecialCapMultiplier=0\n");
            require(helper, dispatcher.execute("infection config reload", source) == 1, "Config reload command succeeds");
            require(helper, OutbreakSpawnRules.effectiveTotalCap(OutbreakLevel.CONTAINED) == 6
                    && OutbreakSpawnRules.effectiveSpecialCap(OutbreakLevel.CONTAINED) == 0,
                    "Live caps reflect reloaded settings");
            level.getServer().setDifficulty(Difficulty.NORMAL, true);
            level.setDayTime(6000);
            BlockPos relative = new BlockPos(3, 2, 3);
            helper.setBlock(relative.below(), Blocks.STONE);
            BlockPos pos = helper.absolutePos(relative);
            require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "Natural-spawn switch blocks attempts");
            require(helper, !OutbreakSpawnRules.hasPopulationRoom(level, pos, ModEntities.RUNNER, OutbreakLevel.CONTAINED),
                    "Zero special cap blocks the first special, even with an empty population");
            var spawned = helper.spawn(ModEntities.INFECTED, new BlockPos(1, 2, 1));
            require(helper, spawned.isAlive(), "Disabling natural spawn does not prevent explicit entity creation");
            spawned.discard();

            var valid = file.current();
            java.nio.file.Files.writeString(file.path(), "densityMultiplier=NaN\n");
            require(helper, dispatcher.execute("infection config reload", source) == 0 && file.current() == valid,
                    "Failed reload leaves the previous settings active");
            java.nio.file.Files.writeString(file.path(), "densityMultiplier=2\ndayCommonMultiplier=1\n");
            require(helper, dispatcher.execute("infection config reload", source) == 1
                    && acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "Valid reload resumes spawning without restart");
            require(helper, dispatcher.execute("infection config show", source) == 1
                    && dispatcher.execute("infection outbreak", source.withPermission(0)) == 1,
                    "Config and public Outbreak queries execute");
        } finally {
            java.nio.file.Files.write(file.path(), original);
            file.reload();
        }
        helper.succeed();
    }

    /** Narrow dev.3 fixture; select this test without rerunning the earlier regression suite. */
    @GameTest(maxTicks = 240, skyAccess = true)
    public void daytimeSpawnAndPolish(GameTestHelper helper) {
        var level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        level.setDayTime(6000);
        level.setWeatherParameters(20000, 0, false, false);
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        helper.runAfterDelay(20, () -> verifyDaytimeRules(helper));
    }

    private void verifyDaytimeRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos relative = new BlockPos(3, 2, 3);
        BlockPos pos = helper.absolutePos(relative);
        List<EntityType<? extends AbstractSpecialInfectedEntity<?>>> types = List.of(
                ModEntities.INFECTED, ModEntities.RUNNER, ModEntities.BLOATER, ModEntities.SPITTER);
        require(helper, level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) == 15,
                "Daytime fixture must receive full skylight");
        for (var type : types) {
            require(helper, acceptsNaturalSpawn(helper, type, pos), "Daylight must allow " + type);
            require(helper, OutbreakSpawnRules.timeOfDayMultiplier(type, 6000)
                    < OutbreakSpawnRules.timeOfDayMultiplier(type, 18000), "Night must have greater pressure");
        }
        level.setDayTime(18000);
        for (var type : types) {
            require(helper, acceptsNaturalSpawn(helper, type, pos), "Night must allow " + type);
        }
        level.getServer().setDifficulty(Difficulty.PEACEFUL, true);
        for (var type : types) {
            require(helper, !acceptsNaturalSpawn(helper, type, pos), "Peaceful must block " + type);
        }
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        level.setDayTime(6000);
        helper.setBlock(relative.below(), Blocks.OAK_LEAVES);
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "Leaves must reject spawning");
        helper.setBlock(relative.below(), Blocks.STONE);
        helper.setBlock(relative, Blocks.WATER);
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "Water must reject spawning");
        helper.setBlock(relative, Blocks.AIR);
        helper.setBlock(relative.above(), Blocks.STONE);
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "One-block space must reject spawning");
        helper.setBlock(relative.above(), Blocks.AIR);
        helper.setBlock(relative.above(2), Blocks.STONE);
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.BLOATER, pos), "Bloater needs its full height");
        helper.setBlock(relative.above(2), Blocks.AIR);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setPos(pos.getX() + 5, pos.getY(), pos.getZ());
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "No spawning within 24 blocks of a player");
        player.discard();

        List<AbstractSpecialInfectedEntity<?>> fixtures = new ArrayList<>();
        fixtures.add(helper.spawn(ModEntities.RUNNER, new BlockPos(1, 2, 1)));
        require(helper, !OutbreakSpawnRules.hasPopulationRoom(level, pos, ModEntities.BLOATER, OutbreakLevel.CONTAINED)
                && OutbreakSpawnRules.hasPopulationRoom(level, pos, ModEntities.INFECTED, OutbreakLevel.CONTAINED),
                "Special cap must leave room for common infected");
        while (fixtures.size() < OutbreakLevel.CONTAINED.localPopulationCap()) {
            fixtures.add(helper.spawn(ModEntities.INFECTED, new BlockPos(1, 2, 1)));
        }
        require(helper, !acceptsNaturalSpawn(helper, ModEntities.INFECTED, pos), "Natural predicate must enforce total cap");
        fixtures.forEach(Entity::discard);
        fixtures.clear();
        for (int i = 0; i < types.size(); i++) {
            var entity = helper.spawn(types.get(i), new BlockPos(1 + i * 2, 2, 1));
            entity.setNoAi(true);
            fixtures.add(entity);
        }
        // Let the real light engine propagate the lamp before checking the artificial-light gate.
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.SEA_LANTERN);
        helper.runAfterDelay(20, () -> {
            require(helper, level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) > 7,
                    "Lamp fixture must actually be brightly lit");
            for (var type : types) {
                require(helper, !acceptsNaturalSpawn(helper, type, pos), "Bright artificial light must reject " + type);
            }
        });
        helper.runAfterDelay(100, () -> {
            for (int i = 0; i < types.size(); i++) {
                var entity = fixtures.get(i);
                require(helper, entity.isAlive() && !entity.isOnFire(), "All four infected must survive daytime sun");
            }
            level.getServer().setDifficulty(Difficulty.PEACEFUL, true);
            helper.runAfterDelay(3, () -> {
                for (int i = 0; i < types.size(); i++) {
                    require(helper, fixtures.get(i).isRemoved(), "Peaceful must remove existing infected");
                }
                level.getServer().setDifficulty(Difficulty.NORMAL, true);
                helper.succeed();
            });
        });
    }

    private boolean acceptsNaturalSpawn(GameTestHelper helper,
            EntityType<? extends net.minecraft.world.entity.monster.Monster> type, BlockPos pos) {
        var random = net.minecraft.util.RandomSource.create(7319);
        for (int attempt = 0; attempt < 2048; attempt++) {
            if (net.minecraft.world.entity.SpawnPlacements.checkSpawnRules(type, helper.getLevel(),
                    net.minecraft.world.entity.EntitySpawnReason.NATURAL, pos, random)) {
                return true;
            }
        }
        return false;
    }

    @GameTest(maxTicks = 200, skyAccess = true)
    public void populationAndKnownRegressions(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        verifyComposition(helper);
        verifySummon(helper);
        verifyPopulationCaps(helper);
        verifyNoise(helper);
        verifyInfectionArithmetic(helper);
        verifySunAndPeaceful(helper);
    }

    private void verifyComposition(GameTestHelper helper) {
        int[][] expected = {
                {0, 92, 5, 2, 1, 12, 1}, {3, 86, 8, 3, 3, 18, 2},
                {7, 78, 12, 5, 5, 24, 3}, {15, 70, 16, 7, 7, 32, 5},
                {30, 62, 20, 9, 9, 40, 7}
        };
        double[] densities = {0.22, 0.32, 0.46, 0.62, 0.78};
        for (int index = 0; index < expected.length; index++) {
            int[] row = expected[index];
            OutbreakLevel level = OutbreakLevel.fromDay(row[0]);
            require(helper, level.index() == index && level.commonWeight() == row[1]
                    && level.runnerWeight() == row[2] && level.bloaterWeight() == row[3]
                    && level.spitterWeight() == row[4] && level.localPopulationCap() == row[5]
                    && level.localSpecialCap() == row[6] && level.densityMultiplier() == densities[index],
                    "Outbreak weights/caps/density at day " + row[0]);
            require(helper, level.commonWeight() > level.runnerWeight() + level.bloaterWeight()
                    + level.spitterWeight(), "Common infected must dominate every stage");
            if (index > 0) {
                require(helper, OutbreakLevel.fromDay(row[0] - 1).index() == index - 1,
                        "Outbreak day boundary");
            }
        }
        require(helper, BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.INFECTED).toString()
                .equals("zombie-infection:infected"), "Real common infected registry ID");
        require(helper, BuiltInRegistries.ITEM.getKey(ModItems.INFECTED_SPAWN_EGG).toString()
                .equals("zombie-infection:infected_spawn_egg"), "Common spawn egg registration");
        VanillaHostileSpawnConfig config = VanillaHostileSpawnConfig.current();
        require(helper, !config.allowsNaturalSpawn(EntityType.ZOMBIE)
                && !config.allowsNaturalSpawn(EntityType.SKELETON)
                && !config.allowsNaturalSpawn(EntityType.CREEPER)
                && config.allowsNaturalSpawn(EntityType.COW)
                && config.allowsNaturalSpawn(EntityType.VILLAGER)
                && config.allowsNaturalSpawn(ModEntities.INFECTED), "Default hostile/passive config");
    }

    private void verifySummon(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        var server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack()
                        .withLevel(helper.getLevel()),
                "summon zombie-infection:infected " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        + " {Tags:[\"zi_regression_summon\"],NoAI:1b}");
        var summoned = helper.getLevel().getEntitiesOfClass(CommonInfectedEntity.class,
                new AABB(pos).inflate(1), entity -> entity.getTags().contains("zi_regression_summon"));
        require(helper, summoned.size() == 1, "Vanilla summon must resolve and create the common infected");
        summoned.forEach(Entity::discard);
    }

    private void verifyPopulationCaps(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        BlockPos absolute = helper.absolutePos(pos);
        for (OutbreakLevel stage : OutbreakLevel.values()) {
            List<Entity> fixtures = new ArrayList<>();
            for (int i = 0; i < stage.localSpecialCap(); i++) {
                fixtures.add(helper.spawn(ModEntities.RUNNER, pos));
            }
            require(helper, !OutbreakSpawnRules.hasPopulationRoom(helper.getLevel(), absolute,
                    ModEntities.SPITTER, stage), "Special cap must include other special types");
            require(helper, OutbreakSpawnRules.hasPopulationRoom(helper.getLevel(), absolute,
                    ModEntities.INFECTED, stage), "Special cap must leave room for common infected");
            while (fixtures.size() < stage.localPopulationCap() - 1) {
                fixtures.add(helper.spawn(ModEntities.INFECTED, pos));
            }
            require(helper, OutbreakSpawnRules.hasPopulationRoom(helper.getLevel(), absolute,
                    ModEntities.INFECTED, stage), "One remaining population slot");
            fixtures.add(helper.spawn(ModEntities.INFECTED, pos));
            require(helper, !OutbreakSpawnRules.hasPopulationRoom(helper.getLevel(), absolute,
                    ModEntities.INFECTED, stage), "Total cap must stop common spawning");
            fixtures.forEach(Entity::discard);
        }
    }

    private void verifyNoise(GameTestHelper helper) {
        var runner = helper.spawn(ModEntities.RUNNER, new BlockPos(7, 2, 1));
        var common = helper.spawn(ModEntities.INFECTED, new BlockPos(3, 2, 5));
        Vec3 origin = runner.position().subtract(7.0, 0, 0);
        NoiseManager.emit(helper.getLevel(), origin, 6, NoiseType.SPRINT, null);
        require(helper, origin.equals(runner.lastNoisePosition()),
                "Runner must hear sprint at seven blocks, beyond the old six-block query");
        require(helper, InfectedHearing.searchRadius(6) == 7.5
                && InfectedHearing.searchRadius(1000) == 46,
                "Noise query covers sensitivity and stays bounded by actual hearing");
        for (NoiseType type : NoiseType.values()) {
            common.clearInvestigation();
            Vec3 near = common.position().subtract(5.0, 0, 0);
            NoiseManager.emit(helper.getLevel(), near, 6, type, null);
            require(helper, near.equals(common.lastNoisePosition()), "Common hearing for " + type);
            common.clearInvestigation();
            NoiseManager.emit(helper.getLevel(), common.position().subtract(5.6, 0, 0), 6, type, null);
            require(helper, common.lastNoisePosition() == null, "Common sensitivity remains individual");
        }
        require(helper, !common.isValidPlayerTarget(runner) && !runner.isValidPlayerTarget(common),
                "Infected must not target one another");
        runner.discard();
        common.discard();
    }

    private void verifyInfectionArithmetic(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        InfectionManager.setInfection(player, 50);
        require(helper, InfectionManager.addInfection(player, Integer.MAX_VALUE) == 100,
                "Large positive infection must saturate at 100 instead of wrapping to zero");
        InfectionManager.setInfection(player, 50);
        require(helper, InfectionManager.addInfection(player, Integer.MIN_VALUE) == 0,
                "Large negative infection must saturate at zero");
        InfectionManager.setInfection(player, 50);
        require(helper, InfectionManager.reduceInfection(player, Integer.MAX_VALUE) == 0,
                "Large treatment must saturate at zero");
        InfectionManager.setInfection(player, 15);
        require(helper, InfectionManager.addInfection(player, 5) == 20
                && InfectionManager.reduceInfection(player, 5) == 15
                && InfectionManager.setInfection(player, 0) == 0, "Normal infection arithmetic");
        player.discard();
    }

    private void verifySunAndPeaceful(GameTestHelper helper) {
        var common = helper.spawn(ModEntities.INFECTED, new BlockPos(1, 2, 1));
        List<AbstractSpecialInfectedEntity<?>> infected = List.of(common,
                helper.spawn(ModEntities.RUNNER, new BlockPos(3, 2, 1)),
                helper.spawn(ModEntities.BLOATER, new BlockPos(5, 2, 1)),
                helper.spawn(ModEntities.SPITTER, new BlockPos(7, 2, 1)));
        require(helper, common.getMaxHealth() == 20 && common.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.24
                && common.getAttributeValue(Attributes.ATTACK_DAMAGE) == 3
                && common.getAttributeValue(Attributes.FOLLOW_RANGE) == 32, "Common base stats");
        infected.forEach(entity -> entity.setNoAi(true));
        Zombie vanilla = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        vanilla.setNoAi(true);
        helper.getLevel().setDayTime(6000);
        helper.getLevel().setWeatherParameters(20000, 0, false, false);
        helper.runAfterDelay(100, () -> {
            for (var entity : infected) {
                require(helper, entity.isAlive() && !entity.isOnFire(), "Mod infected must survive sunlight");
            }
            require(helper, vanilla.isOnFire(), "Vanilla zombie must retain sun sensitivity");
            Difficulty previous = helper.getLevel().getDifficulty();
            helper.getLevel().getServer().setDifficulty(Difficulty.PEACEFUL, true);
            helper.runAfterDelay(3, () -> {
                boolean allRemoved = infected.stream().allMatch(Entity::isRemoved);
                helper.getLevel().getServer().setDifficulty(previous, true);
                require(helper, allRemoved, "All four mod infected must disappear in Peaceful");
                helper.succeed();
            });
        });
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }
}
