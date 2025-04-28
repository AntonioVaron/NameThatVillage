package net.anse.namethatvillage.block.entity;

import net.anse.namethatvillage.init.ModBlockEntities;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VillageBellBlockEntity extends BlockEntity implements MenuProvider {
    private String villageName = "";
    private final List<UUID> villagerIds = new ArrayList<>();
    private int searchCooldown = 0;
    private boolean shaking;
    private int shakeTime;
    private int shakeDirection;

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
        return null;
    }
}