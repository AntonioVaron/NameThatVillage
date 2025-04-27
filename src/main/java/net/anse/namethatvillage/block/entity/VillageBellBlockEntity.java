package net.anse.namethatvillage.block.entity;

import net.anse.namethatvillage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VillageBellBlockEntity extends BlockEntity {
    private String villageName = "";
    private final List<UUID> villagerIds = new ArrayList<>();
    private int searchCooldown = 0;

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
        if (level.isClientSide) return;

        // Reducir el contador de cooldown
        if (blockEntity.searchCooldown > 0) {
            blockEntity.searchCooldown--;
        }
    }

    public void onRing() {
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

    /*
    public void readCustomData(CompoundTag tag) {
        this.villageName = tag.getString("VillageName");

        villagerIds.clear();
        ListTag villagerList = tag.getList("Villagers", Tag.TAG_STRING);
        for (int i = 0; i < villagerList.size(); i++) {
            try {
                villagerIds.add(UUID.fromString(villagerList.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }*/

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        this.villageName = tag.getString("VillageName");

        villagerIds.clear();
        ListTag villagerList = tag.getList("Villagers", Tag.TAG_STRING);
        for (int i = 0; i < villagerList.size(); i++) {
            try {
                villagerIds.add(UUID.fromString(villagerList.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {  // En 1.21.4 es mejor usar saveAdditional
        super.saveAdditional(tag, registries);
        tag.putString("VillageName", villageName);

        // Guardar IDs de aldeanos
        ListTag villagerList = new ListTag();
        for (UUID id : villagerIds) {
            villagerList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("Villagers", villagerList);
    }

    // Getters y setters
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
}