package net.anse.namethatvillage.block.entity;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.VillageBellManager;
import net.anse.namethatvillage.VillageNameGenerator;
import net.anse.namethatvillage.init.ModBlockEntities;
import net.anse.namethatvillage.init.ModBlocks;
import net.anse.namethatvillage.screen.custom.VillageBellMenu;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VillageBellBlockEntity extends BlockEntity implements MenuProvider {
    private String villageName = "Default";
    private final List<UUID> villagerIds = new ArrayList<>();
    private final List<ChunkPos> villageChunks = new ArrayList<>();
    private boolean isPrimary = true;
    private int searchCooldown = 0;
    private boolean shaking;
    private int shakeTime;
    private int shakeDirection;
    private static final Set<Block> VILLAGE_BLOCKS = Set.of(
            Blocks.BELL,
            Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED,
            Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED,
            Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED,
            Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED,
            Blocks.COMPOSTER, Blocks.SMITHING_TABLE, Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE, Blocks.LECTERN, Blocks.STONE_BRICKS,
            Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.OAK_PLANKS, Blocks.BAMBOO_PLANKS,
            Blocks.ACACIA_PLANKS, Blocks.BIRCH_PLANKS, Blocks.CHERRY_PLANKS, Blocks.CRIMSON_PLANKS,
            Blocks.DARK_OAK_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.SPRUCE_PLANKS,
            Blocks.WARPED_PLANKS, Blocks.DIRT_PATH, ModBlocks.VILLAGE_BELL.get()
    );
    private static final Logger LOGGER = LogManager.getLogger();

    public VillageBellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGE_BELL.get(), pos, state);
    }

    public final ItemStackHandler Inventory = new ItemStackHandler(1)
    {
        @Override
        protected int getStackLimit(int slot, ItemStack stack)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if(!level.isClientSide())
            {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public static void tick(Level level, BlockPos pos, BlockState state, VillageBellBlockEntity blockEntity) {
        if (level.isClientSide) {

        // Reducir el contador de cooldown
        if (blockEntity.searchCooldown > 0)
            blockEntity.searchCooldown--;
        }
        if (blockEntity.shaking) {
            ++blockEntity.shakeTime;
            if (blockEntity.shakeTime >= 50) {
                blockEntity.shaking = false;
                blockEntity.shakeTime = 0;
            }
        }
    }

    public void onRing() {

        this.shaking = true;
        this.shakeTime = 0;

        if (level instanceof ServerLevel serverLevel && searchCooldown <= 0) {
            searchCooldown = 200; // 10 segundos de cooldown

            // Encontrar aldeanos cercanos
            AABB searchArea = new AABB(getBlockPos()).inflate(32);
            List<Villager> nearbyVillagers = serverLevel.getEntitiesOfClass(Villager.class, searchArea);

            // Limpiar lista actual
            villagerIds.clear();

            // Agregar nuevos aldeanos
            for (Villager villager : nearbyVillagers) {
                villagerIds.add(villager.getUUID());
            }

            // Registrar como POI de tipo meeting
            PoiManager poiManager = serverLevel.getPoiManager();

            // Obtener el Lookup de POI Types
            var poiTypeLookup = serverLevel.registryAccess().lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE);

            // Clave del POI "meeting"
            ResourceKey<PoiType> meetingKey = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath("minecraft", "meeting"));

            // Obtener el Holder del tipo de POI
            Optional<Holder.Reference<PoiType>> meetingPoiTypeHolder = poiTypeLookup.get(meetingKey);

            // Registrar el POI si está disponible
            meetingPoiTypeHolder.ifPresent(holder -> {
                poiManager.add(getBlockPos(), holder);
            });



            // Marcar para actualizar
            setChanged();
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            this.shakeTime = 0;
            this.shaking = true;
            this.shakeDirection = param-2;
            System.out.println("shakeDirection: " + this.shakeDirection);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public Direction getClickDirection() {
        if (shakeDirection == 0) {
            return Direction.NORTH;  // Sacudida en la dirección norte
        } else if (shakeDirection == 1) {
            return Direction.SOUTH;  // Sacudida en la dirección sur
        } else if (shakeDirection == 2) {
            return Direction.WEST;   // Sacudida en la dirección oeste
        } else if (shakeDirection == 3) {
            return Direction.EAST;   // Sacudida en la dirección este
        }
        return Direction.NORTH;  // Valor por defecto
    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        this.villageName = tag.getString("VillageName");
        this.shakeDirection = tag.getInt("ShakeDirection");

        villagerIds.clear();
        ListTag villagerList = tag.getList("Villagers", Tag.TAG_STRING);
        for (int i = 0; i < villagerList.size(); i++) {
            try {
                villagerIds.add(UUID.fromString(villagerList.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }

        villageChunks.clear();
        ListTag chunkList = tag.getList("VillageChunks", Tag.TAG_COMPOUND);
        for (Tag element : chunkList) {
            if (element instanceof CompoundTag entry) {
                int x = entry.getInt("x");
                int z = entry.getInt("z");
                villageChunks.add(new ChunkPos(x, z));
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        recalcVillageStatus();

    }

    public void recalcVillageStatus() {

        this.villageChunks.clear();

        if (!level.isClientSide) {
            VillageBellManager.registerBell(this);
        }
        // Solo si aún no tiene nombre
        if (villageName.equals("Default") && this.level != null && !this.level.isClientSide) {
            Biome biome = this.level.getBiome(this.worldPosition).value();
            ResourceLocation biomeKey = this.level.registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getKey(biome);

            if (biomeKey != null) {
                String biomePath = biomeKey.getPath(); // por ejemplo "plains", "desert", etc.
                this.villageName = VillageNameGenerator.generateVillageName(biomePath);
                LOGGER.info("Asignado nombre '{}' a campana en {}", villageName, this.worldPosition);

                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        if (this.level != null && !this.level.isClientSide) {
            if (this.villageChunks.isEmpty()) {

                VillageBellBlockEntity overlapping = VillageBellManager.getAllBells().stream()
                        .filter(other -> other != this)
                        .filter(other -> other.getLevel() == this.getLevel())
                        .filter(other -> other.isPrimary)
                        .filter(other -> other.getVillageChunks().contains(new ChunkPos(this.getBlockPos())))
                        .findFirst()
                        .orElse(null);

                if (overlapping != null) {
                    this.villageName = overlapping.getVillageName();
                    this.isPrimary = false;
                    LOGGER.info("Campana en {} se marca como SUPLETORIA por estar dentro de aldea '{}'", this.getBlockPos(), this.villageName);
                } else {
                    // No pertenece a ninguna aldea → se convierte en principal
                    LOGGER.info("Campana en {} se marca como PRINCIPAL y calcula su aldea", this.getBlockPos());
                    this.calcVillageChunks();
                    this.isPrimary = true;
                }

            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        VillageBellManager.unregisterBell(this);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("VillageName", villageName);
        this.shakeDirection = tag.getInt("ShakeDirection");

        // Guardar IDs de aldeanos
        ListTag villagerList = new ListTag();
        for (UUID id : villagerIds) {
            villagerList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("Villagers", villagerList);

        // Chunk list
        ListTag chunkList = new ListTag();
        for (ChunkPos pos : villageChunks) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", pos.x);
            entry.putInt("z", pos.z);
            chunkList.add(entry);
        }
        tag.put("VillageChunks", chunkList);
    }

    public void calcVillageChunks()
    {
        Set<ChunkPos> checkedChunks = new HashSet<>();
        Queue<ChunkPos> unexploredChunks = new ArrayDeque<>();

        ChunkPos startChunk = new ChunkPos(this.getBlockPos());
        unexploredChunks.add(startChunk);

        while (!unexploredChunks.isEmpty()) {
            ChunkPos current = unexploredChunks.poll();

            // Si ya fue evaluado, salta
            if (!checkedChunks.add(current)) {
                continue;
            }

            // Si ya pertenece a otra aldea, ignorar
            if (chunkBelongsToOther(current)) {
                continue;
            }

            // Si contiene bloques de aldea, lo añadimos y expandimos
            if (chunkHasVillageBlocks(current)) {

                LOGGER.info("Chunk {} contiene bloques de aldea", current);
                if (!villageChunks.contains(current)) {
                    villageChunks.add(current);
                }

                // Añadir los 8 vecinos como parte de la aldea (sin bloquear expansión)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;

                        ChunkPos vecino = new ChunkPos(current.x + dx, current.z + dz);
                        if (!villageChunks.contains(vecino)) {
                            villageChunks.add(vecino);
                            unexploredChunks.add(vecino); // ⚠️ Esto es clave para que puedan expandirse
                            LOGGER.info("Se añadió chunk vecino {} como parte de la aldea desde {}", vecino, current);
                        }
                    }
                }
            }
        }
        LOGGER.info("Aldea '{}' tiene {} chunks: {}", villageName, villageChunks.size(), villageChunks);
    }

    private boolean chunkBelongsToOther(ChunkPos chunkPos) {
        for (VillageBellBlockEntity other : VillageBellManager.getAllBells()) {
            if (other == this) continue;
            if (other.villageChunks.contains(chunkPos)) {
                return true;
            }
        }
        return false;
    }


    private boolean chunkHasVillageBlocks(ChunkPos chunkPos) {
        if (this.level == null) return false;

        LevelChunk chunk = this.level.getChunk(chunkPos.x, chunkPos.z);
        BlockPos chunkOrigin = chunkPos.getWorldPosition();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

                int verticalRadius = 2 * 16;

                int minY = Math.max(this.level.getMinY(), surfaceY - verticalRadius);
                int maxY = Math.min(this.level.getMaxY(), surfaceY + verticalRadius);

                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = chunkOrigin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (VILLAGE_BLOCKS.contains(state.getBlock())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Getters y setters
    public List<ChunkPos> getVillageChunks() {
        return this.villageChunks;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
        setChanged();
    }

    public List<UUID> getVillagerIds() {
        return villagerIds;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }

    public int getShakeTime() {
        return this.shakeTime;
    }

    public int getShakeDirection() {
        return this.shakeDirection;
    }

    public boolean isShaking() {
        return shaking;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Village");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VillageBellMenu(containerId, playerInventory, this);
    }
}