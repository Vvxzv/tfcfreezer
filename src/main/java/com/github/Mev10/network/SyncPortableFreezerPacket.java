package com.github.Mev10.network;

import com.github.Mev10.client.screen.PortableFreezerScreen;
import com.github.Mev10.common.container.PortableFreezerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SyncPortableFreezerPacket {
    private final ItemStack stack;

    public SyncPortableFreezerPacket(ItemStack stack) {
        this.stack = stack;
    }

    public static void encode(SyncPortableFreezerPacket msg, FriendlyByteBuf buf) {
        writeItemStack(msg.stack, buf);
    }

    public static SyncPortableFreezerPacket decode(FriendlyByteBuf buf) {
        return new SyncPortableFreezerPacket(readItemStack(buf));
    }

    public static void handle(SyncPortableFreezerPacket msg, ServerPlayer player) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> clientHandle(msg));
    }

    private static void clientHandle(SyncPortableFreezerPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CompoundTag receivedTag = msg.stack.getOrCreateTag();

        if (mc.player.containerMenu instanceof PortableFreezerContainer container) {
            container.receiveSyncData(receivedTag);

            // 强制刷新所有冰箱槽位
            for (int i = 0; i < PortableFreezerContainer.SLOTS; i++) {
                container.slots.get(i).setChanged();
            }

            // 刷新屏幕
            if (mc.screen instanceof PortableFreezerScreen screen) {
                screen.refresh();
            }
        }
    }

    public static void writeItemStack(ItemStack stack, FriendlyByteBuf packetBuffer) {
        if (stack.isEmpty()) {
            packetBuffer.writeBoolean(false);
        } else {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeNbt(stack.save(new CompoundTag()));
            packetBuffer.writeInt(stack.getCount());
        }
    }

    public static ItemStack readItemStack(FriendlyByteBuf packetBuffer) {
        if (!packetBuffer.readBoolean()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = packetBuffer.readNbt();
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        int count = packetBuffer.readInt();
        ItemStack stack = ItemStack.of(tag);
        stack.setCount(count);
        return stack;
    }
}