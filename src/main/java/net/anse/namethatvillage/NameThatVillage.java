package net.anse.namethatvillage;

import net.anse.namethatvillage.block.VillageBellBlock;
import net.anse.namethatvillage.init.ModBlockEntities;
import net.anse.namethatvillage.init.ModBlocks;
import net.anse.namethatvillage.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Mod(NameThatVillage.MOD_ID)
public class NameThatVillage {
    public static final String MOD_ID = "namethatvillage";
    private static final Logger LOGGER = LogManager.getLogger();

    public NameThatVillage(IEventBus modEventBus) {  // Recibe el IEventBus directamente
        LOGGER.info("Name That Village mod initialized!");

        // Registrar componentes usando el bus de eventos del mod
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        modEventBus.addListener(this::onRegisterItems);

        // Registrar el manejador de eventos del juego
        NeoForge.EVENT_BUS.register(this);
    }

    // Evento de colocación de bloque
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        BlockState state = event.getPlacedBlock();
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        if (state.getBlock() instanceof VillageBellBlock && level instanceof ServerLevel serverLevel) {
            LOGGER.info("Village Bell placed at {}, registering as POI", pos);

            // Registrar como POI
            var poiTypeLookup = serverLevel.registryAccess().lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE);
            ResourceKey<PoiType> meetingKey = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "meeting"));

            Optional<Holder.Reference<PoiType>> meetingPoiTypeHolder = poiTypeLookup.get(meetingKey);
            meetingPoiTypeHolder.ifPresent(holder -> {
                serverLevel.getPoiManager().add(pos, holder);
                LOGGER.info("Successfully registered Village Bell as meeting POI at {}", pos);
            });
        }
    }

    // Evento de rotura de bloque
    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        if (state.getBlock() instanceof VillageBellBlock && level instanceof ServerLevel serverLevel) {
            LOGGER.info("Village Bell broken at {}, removing POI", pos);

            // Eliminar el POI
            serverLevel.getPoiManager().remove(pos);
        }
    }
    //@SubscribeEvent
    public void onRegisterItems(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            event.register(Registries.ITEM, helper -> {
                helper.register(ResourceLocation.fromNamespaceAndPath(NameThatVillage.MOD_ID, "village_bell"),
                        new BlockItem(ModBlocks.VILLAGE_BELL.get(), new Item.Properties()));
            });
        }
    }
}