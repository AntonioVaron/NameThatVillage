package net.anse.namethatvillage.init;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

import java.util.Set;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, NameThatVillage.MOD_ID);

    public static final Supplier<BlockEntityType<VillageBellBlockEntity>> VILLAGE_BELL = BLOCK_ENTITIES.register(
            "village_bell",
            () -> new BlockEntityType<>(
                    VillageBellBlockEntity::new,
                    ModBlocks.VILLAGE_BELL.get(), null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}