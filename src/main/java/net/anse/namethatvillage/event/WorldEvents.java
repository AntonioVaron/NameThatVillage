package net.anse.namethatvillage.event;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.VillageBellManager;
import net.anse.namethatvillage.attachment.ModAttachments;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.anse.namethatvillage.data.VillageBellChunkData;
import net.anse.namethatvillage.network.NetworkHandler;
import net.anse.namethatvillage.network.packet.ShowVillageTitlePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.server.level.ServerLevel;

import java.util.*;


import net.anse.namethatvillage.init.ModBlocks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldEvents {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ServerPlayer, ChunkPos> LAST_CHUNK = new HashMap<>();
    private static final Map<ServerPlayer, String> LAST_VILLAGE = new HashMap<>();


    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {

        Level level = (Level) event.getLevel();
        LevelChunk chunk = (LevelChunk) event.getChunk();
        VillageBellChunkData data = chunk.getData(ModAttachments.VILLAGE_BELL_CHUNK);

        if (data.isReplaced()) return;
        if (level.isClientSide()) return;

        // Cast para acceder a estructuras
        if (!(level instanceof ServerLevel serverLevel)) return;

        Registry<Structure> structureRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        StructureManager structureManager = serverLevel.structureManager();

        boolean isVillageChunk = chunk.getAllStarts().entrySet().stream()
                .anyMatch(entry -> {
                    StructureStart start = entry.getValue();
                    ResourceLocation key = structureRegistry.getKey(entry.getKey());
                    return key != null
                            && key.getPath().contains("village")
                            && start != StructureStart.INVALID_START;
                });

        if (!isVillageChunk) {
            //LOGGER.debug("Chunk {} no contiene StructureStart de aldea, omitiendo procesamiento.", chunk.getPos());
            return;
        }

        LOGGER.info("Chunk {} contiene StructureStart de aldea, procesando área 3x3...", chunk.getPos());

        boolean foundBell = false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos nearbyPos = new ChunkPos(chunk.getPos().x + dx, chunk.getPos().z + dz);
                LevelChunk nearbyChunk = serverLevel.getChunk(nearbyPos.x, nearbyPos.z);
                LOGGER.info("llega llega, tranquilo");

                BlockPos origin = nearbyChunk.getPos().getWorldPosition();

                for (int x = origin.getX(); x < origin.getX() + 16; x++) {
                    for (int z = origin.getZ(); z < origin.getZ() + 16; z++) {
                        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(pos);

                            if (state.getBlock() == Blocks.BELL) {
                                LOGGER.info("Reemplazando campana vanilla en {}", pos);
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
                                foundBell = true;
                            }
                        }
                    }
                }
            }
        }

        // Si se reemplazó alguna campana, marcamos los 9 chunks como procesados
        if (foundBell) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ChunkPos nearbyPos = new ChunkPos(chunk.getPos().x + dx, chunk.getPos().z + dz);
                    LevelChunk markChunk = serverLevel.getChunkSource().getChunkNow(nearbyPos.x, nearbyPos.z);
                    if (markChunk == null) continue;

                    data = markChunk.getData(ModAttachments.VILLAGE_BELL_CHUNK);
                    if (!data.isReplaced()) {
                        data.markReplaced();
                        LOGGER.info("Marcado chunk {} como procesado", markChunk.getPos());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChunkPos currentChunk = new ChunkPos(player.blockPosition());
        ChunkPos lastChunk = LAST_CHUNK.get(player);

        if (currentChunk.equals(lastChunk)) return;
        LAST_CHUNK.put(player, currentChunk);

        LOGGER.info("Jugador {} ha cambiado al chunk {}", player.getName().getString(), currentChunk);

        for (VillageBellBlockEntity bell : VillageBellManager.getAllBells()) {

            LOGGER.debug("Evaluando campana en {}, nombre: {}", bell.getBlockPos(), bell.getVillageName());
            if (bell.getLevel() == null) continue;
            if (!bell.getLevel().dimension().equals(player.level().dimension())) continue;

            if (bell.getVillageChunks().contains(currentChunk)) {
                String currentVillage = bell.getVillageName();
                String lastVillage = LAST_VILLAGE.get(player);
                LOGGER.info("Jugador {} ha entrado en aldea '{}'", player.getName().getString(), currentVillage);
                // Solo mostrar si ha cambiado de aldea
                if (!currentVillage.equals(lastVillage)) {
                    LAST_VILLAGE.put(player, currentVillage);
                    LOGGER.debug("Enviando título '{}' al jugador {}", currentVillage, player.getName().getString());
                    NetworkHandler.sendTitleTo(player, new ShowVillageTitlePacket(currentVillage));
                }

                return;
            }
        }
        LAST_VILLAGE.remove(player);
    }


}
