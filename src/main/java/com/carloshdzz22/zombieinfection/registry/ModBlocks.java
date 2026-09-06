package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.block.MedicalLaboratoryBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class ModBlocks {
    public static final ResourceKey<Block> MEDICAL_LABORATORY_KEY = ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, ZombieInfection.id("medical_laboratory"));
    public static final Block MEDICAL_LABORATORY = new MedicalLaboratoryBlock(Block.Properties.of().setId(MEDICAL_LABORATORY_KEY).strength(2.5F).requiresCorrectToolForDrops());

    private ModBlocks() {}

    public static void initialize() {
        register("medical_laboratory", MEDICAL_LABORATORY);
        ZombieInfection.LOGGER.info("Registered Zombie Infection blocks");
    }

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, ZombieInfection.id(name), block);
    }
}
