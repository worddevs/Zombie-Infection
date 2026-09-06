package com.carloshdzz22.zombieinfection.worldgen.structure;

import com.carloshdzz22.zombieinfection.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;

/** Small ruined pharmacy with restrained medical loot. */
public final class AbandonedPharmacyPiece extends ScatteredFeaturePiece {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 6;
    public static final int DEPTH = 7;

    public AbandonedPharmacyPiece(RandomSource random, int x, int z) {
        super(ModStructures.ABANDONED_PHARMACY_PIECE, x, 64, z,
                WIDTH, HEIGHT, DEPTH, getRandomHorizontalDirection(random));
    }

    public AbandonedPharmacyPiece(CompoundTag tag) {
        super(ModStructures.ABANDONED_PHARMACY_PIECE, tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
            ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
            ChunkPos chunkPos, BlockPos pivot) {
        if (!updateAverageGroundHeight(level, chunkBox, 0)) {
            return;
        }

        BlockState foundation = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState wall = Blocks.WHITE_TERRACOTTA.defaultBlockState();
        BlockState damaged = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        generateBox(level, chunkBox, 0, 0, 0, WIDTH - 1, 0, DEPTH - 1,
                foundation, foundation, false);
        generateBox(level, chunkBox, 0, 1, 0, WIDTH - 1, 1, DEPTH - 1,
                Blocks.GRAY_CONCRETE.defaultBlockState(),
                Blocks.GRAY_CONCRETE.defaultBlockState(), false);
        generateBox(level, chunkBox, 0, 2, 0, WIDTH - 1, 4, DEPTH - 1,
                wall, damaged, false);
        generateAirBox(level, chunkBox, 1, 2, 1, WIDTH - 2, 4, DEPTH - 2);
        generateBox(level, chunkBox, 0, 5, 0, WIDTH - 1, 5, DEPTH - 1,
                Blocks.DEEPSLATE_TILES.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState(), false);

        placeBlock(level, air, 4, 2, 0, chunkBox);
        placeBlock(level, air, 4, 3, 0, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 1, 3, 0, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 2, 3, 0, chunkBox);
        placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 6, 3, 0, chunkBox);
        placeBlock(level, air, 7, 3, 0, chunkBox);

        generateBox(level, chunkBox, 1, 2, 3, 6, 2, 3,
                Blocks.SMOOTH_STONE_SLAB.defaultBlockState(),
                Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
        placeBlock(level, Blocks.BREWING_STAND.defaultBlockState(), 2, 3, 3, chunkBox);
        placeBlock(level, Blocks.CAULDRON.defaultBlockState(), 6, 2, 5, chunkBox);
        placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 1, 2, 5, chunkBox);
        placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 1, 3, 5, chunkBox);
        placeBlock(level, Blocks.BARREL.defaultBlockState(), 2, 2, 5, chunkBox);
        placeBlock(level, Blocks.BARREL.defaultBlockState(), 3, 2, 5, chunkBox);
        placeBlock(level, Blocks.COBWEB.defaultBlockState(), 7, 4, 5, chunkBox);
        placeBlock(level, Blocks.COBWEB.defaultBlockState(), 1, 4, 1, chunkBox);
        placeBlock(level, Blocks.REDSTONE_TORCH.defaultBlockState(), 7, 2, 1, chunkBox);

        createChest(level, chunkBox, random, 6, 2, 2, ModStructures.ABANDONED_PHARMACY_LOOT);

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                fillColumnDown(level, foundation, x, -1, z, chunkBox);
            }
        }
    }
}
