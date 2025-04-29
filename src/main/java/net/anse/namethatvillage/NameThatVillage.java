package net.anse.namethatvillage;

import net.anse.namethatvillage.block.VillageBellBlock;
import net.anse.namethatvillage.block.entity.renderer.VillageBellBlockEntityRenderer;
import net.anse.namethatvillage.init.ModBlockEntities;
import net.anse.namethatvillage.init.ModBlocks;
import net.anse.namethatvillage.init.ModItems;
import net.anse.namethatvillage.screen.ModMenuTypes;
import net.anse.namethatvillage.screen.custom.VillageBellMenu;
import net.anse.namethatvillage.screen.custom.VillageBellScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


@Mod(NameThatVillage.MOD_ID)
public class NameThatVillage {
    public static final String MOD_ID = "namethatvillage";
    private static final Logger LOGGER = LogManager.getLogger();


    public NameThatVillage(IEventBus modEventBus) {
        LOGGER.info("Name That Village mod initialized!");

        modEventBus.addListener(this::commonSetup);

        // Registrar el manejador de eventos del juego
        NeoForge.EVENT_BUS.register(this);

        // Registrar componentes usando el bus de eventos del mod
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(ModBlocks.VILLAGE_BELL);
        }
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

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerBlockEntityRenderer(
                    ModBlockEntities.VILLAGE_BELL.get(), VillageBellBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event)
        {
            event.register(ModMenuTypes.VILLAGE_BELL_MENU.get(), VillageBellScreen::new);
        }
    }

}