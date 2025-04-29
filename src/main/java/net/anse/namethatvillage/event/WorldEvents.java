package net.anse.namethatvillage.event;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.attachment.ModAttachments;
import net.anse.namethatvillage.data.VillageBellChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;


import net.anse.namethatvillage.init.ModBlocks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldEvents {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LOGGER.info("Chunk loaded: {}", event.getChunk().getPos());

        Level level = (Level) event.getLevel();
        LevelChunk chunk = (LevelChunk) event.getChunk();
        VillageBellChunkData data = chunk.getData(ModAttachments.VILLAGE_BELL_CHUNK);

        if (data.isReplaced()) return;

        if (level.isClientSide()) return;


        // Revisamos todas las posiciones del chunk
        chunk = (LevelChunk) event.getChunk();
        BlockPos chunkOrigin = chunk.getPos().getWorldPosition();


        int chunkX = chunkOrigin.getX();
        int chunkZ = chunkOrigin.getZ();

        LOGGER.info("Scanning chunk from ({}, {})", chunkX, chunkZ);

        boolean foundBell = false;

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = level.getMinY(); y < level.getMaxY(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.getBlock() == Blocks.BELL) {

                        LOGGER.info("Found bell at {}, replacing...", pos);
                        BlockState newState = ModBlocks.VILLAGE_BELL.get().defaultBlockState();

                        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                            newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING,
                                    state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                        }
                        if (state.hasProperty(BlockStateProperties.BELL_ATTACHMENT)) {
                            newState = newState.setValue(BlockStateProperties.BELL_ATTACHMENT,
                                    state.getValue(BlockStateProperties.BELL_ATTACHMENT));
                        }
                        if (state.hasProperty(BlockStateProperties.POWERED)) {
                            newState = newState.setValue(BlockStateProperties.POWERED,
                                    state.getValue(BlockStateProperties.POWERED));
                        }

                        level.setBlock(pos, newState, 3);
                        LOGGER.info("Replaced bell at {}", pos);
                        foundBell = true;
                    }
                }
            }
        }

        if(foundBell) {
            data.markReplaced();
            LOGGER.info("Marked chunk {} as processed", chunk.getPos());
        }
    }
}
