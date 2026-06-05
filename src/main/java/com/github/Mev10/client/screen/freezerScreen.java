package com.github.Mev10.client.screen;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.client.screen.button.freezerTurnOnButton;
import com.github.Mev10.common.TfcfreezerHelpers;
import com.github.Mev10.common.blockentities.freezerBlockEntity;
import com.github.Mev10.common.container.freezerContainer;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class freezerScreen extends BlockEntityScreen<freezerBlockEntity, freezerContainer> {

    public static final ResourceLocation BACKGROUND = TfcfreezerHelpers.identifier("textures/gui/freezer.png");
    private static final int ENERGY_COLOR = 0xFF7DEBFF;
    private static final Component TOGGLE = Component.translatable(String.format("tooltip.%s.toggle", Tfcfreezer.MOD_ID));

    public freezerScreen(freezerContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, BACKGROUND);
        imageWidth += 20;
    }

    @Override
    public void init()
    {
        super.init();
        addRenderableWidget(new freezerTurnOnButton(blockEntity, getGuiLeft(), getGuiTop(), TOGGLE));
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(gui, partialTicks, mouseX, mouseY);
        renderEnergyBar(gui);
    }

    private void renderEnergyBar(GuiGraphics gui)
    {
        int energyScaled = this.menu.getEnergyStoredScaled();
        gui.fill(this.leftPos + 177,
                this.topPos + 40+(64-energyScaled),
                this.leftPos + 185,
                this.topPos + 104,
                ENERGY_COLOR);
    }
}
