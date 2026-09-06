package com.carloshdzz22.zombieinfection.worldgen.structure;

import com.carloshdzz22.zombieinfection.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class AbandonedPharmacyStructure extends SinglePieceStructure {
    public static final MapCodec<AbandonedPharmacyStructure> CODEC =
            Structure.simpleCodec(AbandonedPharmacyStructure::new);

    public AbandonedPharmacyStructure(StructureSettings settings) {
        super(AbandonedPharmacyPiece::new, AbandonedPharmacyPiece.WIDTH,
                AbandonedPharmacyPiece.DEPTH, settings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ABANDONED_PHARMACY;
    }
}
