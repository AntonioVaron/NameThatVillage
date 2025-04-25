package net.anse.namethatvillage.init;

import net.anse.namethatvillage.NameThatVillage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, NameThatVillage.MOD_ID);

    /*// Registramos el BlockItem para nuestro Village Bell
    public static final Supplier<Item> VILLAGE_BELL = ITEMS.register("village_bell",
            () -> new BlockItem(ModBlocks.VILLAGE_BELL.get(), new Item.Properties()));*/

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}