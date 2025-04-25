package net.anse.namethatvillage.init;

import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.block.VillageBellBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, NameThatVillage.MOD_ID);

    public static final Supplier<Block> VILLAGE_BELL = BLOCKS.register("village_bell",
            () -> new VillageBellBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}