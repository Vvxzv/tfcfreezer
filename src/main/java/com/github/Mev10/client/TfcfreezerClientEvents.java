package com.github.Mev10.client;

import com.github.Mev10.client.screen.freezerScreen;
import com.github.Mev10.client.screen.PortableFreezerScreen;
import com.github.Mev10.client.render.FreezerBlockEntityRenderer;
import com.github.Mev10.common.blockentities.TfcfreezerBlocksEntities;
import com.github.Mev10.common.container.TfcfreezerContainers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TfcfreezerClientEvents {
    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(TfcfreezerClientEvents::clientSetup);
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(TfcfreezerContainers.freezer_CONTAINER.get(), freezerScreen::new);
            MenuScreens.register(TfcfreezerContainers.PORTABLE_FREEZER_CONTAINER.get(), PortableFreezerScreen::new);
            BlockEntityRenderers.register(TfcfreezerBlocksEntities.freezer_BLOCK.get(), FreezerBlockEntityRenderer::new);
        });
    }
}
