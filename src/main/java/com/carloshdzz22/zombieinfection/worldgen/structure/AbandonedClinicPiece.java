package com.carloshdzz22.zombieinfection.worldgen.structure;

import com.carloshdzz22.zombieinfection.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;

/** A compact, one-piece clinic intended for ordinary Overworld exploration. */
public final class AbandonedClinicPiece extends ScatteredFeaturePiece {
    public static final int WIDTH = 11;
    public static final int HEIGHT = 7;
    public static final int DEPTH = 9;

    public AbandonedClinicPiece(RandomSource random, int x, int z) {
        super(ModStructures.ABANDONED_CLINIC_PIECE, x, 64, z,
                WIDTH, HEIGHT, DEPTH, getRandomHorizontalDirection(random));
    }

    public AbandonedClinicPiece(CompoundTag tag) {
        super(ModStructures.ABANDONED_CLINIC_PIECE, tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
            ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
            ChunkPos chunkPos, BlockPos pivot) {
        if (!updateAverageGroundHeight(level, chunkBox, 0)) {
            return;
        }

        BlockState foundation = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState floor = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState damagedWall = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState roof = Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        generateBox(level, chunkBox, 0, 0, 0, WIDTH - 1, 0, DEPTH - 1,
                foundation, foundation, false);
        generateBox(level, chunkBox, 0, 1, 0, WIDTH - 1, 1, DEPTH - 1,
                floor, floor, false);
        generateBox(level, chunkBox, 0, 2, 0, WIDTH - 1, 5, DEPTH - 1,
                wall, damagedWall, false);
        generateAirBox(level, chunkBox, 1, 2, 1, WIDTH - 2, 5, DEPTH - 2);
        generateBox(level, chunkBox, 0, 6, 0, WIDTH - 1, 6, DEPTH - 1,
                roof, roof, false);

        placeBlock(level, air, 5, 2, 0, chunkBox);
        placeBlock(level, air, 5, 3, 0, chunkBox);
        placeBlock(level, Blocks.IRON_BARS.defaultBlockState(), 2, 3, 0, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 3, 3, 0, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 7, 3, 0, chunkBox);
        placeBlock(level, air, 8, 3, 0, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 0, 3, 4, chunkBox);
        placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), WIDTH - 1, 3, 4, chunkBox);

        placeBed(level, chunkBox, 2, 2, 2, Direction.SOUTH);
        placeBed(level, chunkBox, 4, 2, 2, Direction.SOUTH);
        generateBox(level, chunkBox, 7, 2, 2, 9, 2, 2,
                Blocks.SMOOTH_STONE_SLAB.defaultBlockState(),
                Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
        placeBlock(level, Blocks.BREWING_STAND.defaultBlockState(), 8, 3, 2, chunkBox);
        placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 9, 2, 5, chunkBox);
        placeBlock(level, Blocks.BOOKSHELF.defaultBlockState(), 9, 3, 5, chunkBox);
        placeBlock(level, Blocks.BARREL.defaultBlockState(), 9, 2, 6, chunkBox);

        placeBlock(level, Blocks.COBWEB.defaultBlockState(), 1, 4, 7, chunkBox);
        placeBlock(level, Blocks.COBWEB.defaultBlockState(), 8, 5, 1, chunkBox);
        placeBlock(level, Blocks.REDSTONE_TORCH.defaultBlockState(), 5, 2, 7, chunkBox);
        placeBlock(level, damagedWall, 1, 2, 0, chunkBox);
        placeBlock(level, air, 9, 4, DEPTH - 1, chunkBox);

        createChest(level, chunkBox, random, 7, 2, 6, ModStructures.ABANDONED_CLINIC_LOOT);

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                fillColumnDown(level, foundation, x, -1, z, chunkBox);
            }
        }
    }

    private void placeBed(WorldGenLevel level, BoundingBox box, int x, int y, int z,
            Direction facing) {
        BlockState foot = Blocks.WHITE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = foot.setValue(BedBlock.PART, BedPart.HEAD);
        placeBlock(level, foot, x, y, z, box);
        placeBlock(level, head, x + facing.getStepX(), y, z + facing.getStepZ(), box);
    }
}
