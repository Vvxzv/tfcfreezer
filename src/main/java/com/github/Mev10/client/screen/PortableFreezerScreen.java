package com.github.Mev10.client.screen;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.client.screen.button.PortableFreezerTurnOnButton;
import com.github.Mev10.common.TfcfreezerHelpers;
import com.github.Mev10.common.container.PortableFreezerContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PortableFreezerScreen extends AbstractContainerScreen<PortableFreezerContainer> {
    public static final ResourceLocation BACKGROUND = TfcfreezerHelpers.identifier("textures/gui/portable_freezer.png");
    private static final int ENERGY_COLOR = 0xFF7DEBFF;

    public PortableFreezerScreen(PortableFreezerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176 + 20;
        this.imageHeight = 148;
        this.inventoryLabelY = 54;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new PortableFreezerTurnOnButton(this.leftPos + 171, this.topPos + 17, menu));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND);
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int energyHeight = menu.getEnergyStoredScaled(64);
        graphics.fill(leftPos + 177, topPos + 40 + (64 - energyHeight),
                leftPos + 185, topPos + 104, ENERGY_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    public void refresh() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.containerMenu.slotsChanged(minecraft.player.getInventory());
        }
    }
}
