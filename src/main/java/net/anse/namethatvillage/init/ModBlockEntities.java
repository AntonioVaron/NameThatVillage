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
            () -> {
                BlockEntityType.BlockEntitySupplier<VillageBellBlockEntity> supplier =
                        (pos, state) -> new VillageBellBlockEntity(pos, state);

                // Registramos con un conjunto vacío de bloques
                return new BlockEntityType<>(supplier, Set.of(), false);
            }
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);

        // Usar el evento específico para añadir bloques a un BlockEntityType
        eventBus.addListener(ModBlockEntities::addBlocksToBlockEntities);
    }

    // Método para manejar el evento que agrega bloques a BlockEntityType
    private static void addBlocksToBlockEntities(BlockEntityTypeAddBlocksEvent event) {
        event.modify(VILLAGE_BELL.get(), ModBlocks.VILLAGE_BELL.get());
    }
}