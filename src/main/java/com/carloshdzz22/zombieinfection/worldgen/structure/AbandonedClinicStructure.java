package com.carloshdzz22.zombieinfection.worldgen.structure;

import com.carloshdzz22.zombieinfection.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class AbandonedClinicStructure extends SinglePieceStructure {
    public static final MapCodec<AbandonedClinicStructure> CODEC =
            Structure.simpleCodec(AbandonedClinicStructure::new);

    public AbandonedClinicStructure(StructureSettings settings) {
        super(AbandonedClinicPiece::new, AbandonedClinicPiece.WIDTH,
                AbandonedClinicPiece.DEPTH, settings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ABANDONED_CLINIC;
    }
}
