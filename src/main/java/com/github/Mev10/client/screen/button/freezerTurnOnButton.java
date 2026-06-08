package com.github.Mev10.client.screen.button;

import com.github.Mev10.client.screen.freezerScreen;
import com.github.Mev10.common.container.freezerContainer;
import com.github.Mev10.network.ApplianceButtonPacket;
import com.github.Mev10.network.TfcfreezerPacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

public class freezerTurnOnButton extends Button {
    private final freezerContainer freezer;

    public freezerTurnOnButton(freezerContainer freezer, int guiLeft, int guiTop, Component tooltip)
    {
        super(guiLeft + 171, guiTop + 17, 20, 20, tooltip, b -> {}, DEFAULT_NARRATION);
        setTooltip(Tooltip.create(tooltip));
        this.freezer = freezer;
    }

    @Override
    public void onPress()
    {
        TfcfreezerPacketHandler.send(PacketDistributor.SERVER.noArg(), new ApplianceButtonPacket(0, null));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        final int v = freezer.isTurnedOn() ? 0 : 20;

        graphics.blit(freezerScreen.BACKGROUND, getX(), getY(), 236, v, 20, 20, 256, 256);
    }
}
