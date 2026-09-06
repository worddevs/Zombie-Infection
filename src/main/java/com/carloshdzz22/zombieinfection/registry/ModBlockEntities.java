package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.block.entity.MedicalLaboratoryBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<MedicalLaboratoryBlockEntity> MEDICAL_LABORATORY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZombieInfection.id("medical_laboratory"),
            FabricBlockEntityTypeBuilder.create(MedicalLaboratoryBlockEntity::new, ModBlocks.MEDICAL_LABORATORY).build()
    );

    private ModBlockEntities() {}

    public static void initialize() {
        ZombieInfection.LOGGER.info("Registered Zombie Infection block entities");
    }
}
