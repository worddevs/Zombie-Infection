package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.worldgen.structure.AbandonedClinicPiece;
import com.carloshdzz22.zombieinfection.worldgen.structure.AbandonedClinicStructure;
import com.carloshdzz22.zombieinfection.worldgen.structure.AbandonedPharmacyPiece;
import com.carloshdzz22.zombieinfection.worldgen.structure.AbandonedPharmacyStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModStructures {
    public static final StructurePieceType.ContextlessType ABANDONED_CLINIC_PIECE =
            AbandonedClinicPiece::new;
    public static final StructurePieceType.ContextlessType ABANDONED_PHARMACY_PIECE =
            AbandonedPharmacyPiece::new;

    public static final StructureType<AbandonedClinicStructure> ABANDONED_CLINIC =
            () -> AbandonedClinicStructure.CODEC;
    public static final StructureType<AbandonedPharmacyStructure> ABANDONED_PHARMACY =
            () -> AbandonedPharmacyStructure.CODEC;

    public static final ResourceKey<LootTable> ABANDONED_CLINIC_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ZombieInfection.id("chests/abandoned_clinic"));
    public static final ResourceKey<LootTable> ABANDONED_PHARMACY_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ZombieInfection.id("chests/abandoned_pharmacy"));

    private ModStructures() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                ZombieInfection.id("abandoned_clinic"), ABANDONED_CLINIC_PIECE);
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                ZombieInfection.id("abandoned_pharmacy"), ABANDONED_PHARMACY_PIECE);
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                ZombieInfection.id("abandoned_clinic"), ABANDONED_CLINIC);
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
                ZombieInfection.id("abandoned_pharmacy"), ABANDONED_PHARMACY);
        ZombieInfection.LOGGER.info("Registered Zombie Infection medical structures");
    }
}
