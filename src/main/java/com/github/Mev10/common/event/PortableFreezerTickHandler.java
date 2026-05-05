package com.github.Mev10.common.event;

import com.github.Mev10.common.container.PortableFreezerContainer;
import com.github.Mev10.common.item.PortablefreezerItem;
import com.github.Mev10.common.item.TfcfreezerFoodTraits;
import com.github.Mev10.network.SyncPortableFreezerPacket;
import com.github.Mev10.network.TfcfreezerPacketHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class PortableFreezerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide) return;
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().getGameTime() % 20 != 0) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PortablefreezerItem) {
                if (PortablefreezerItem.isTurnedOn(stack)) {
                    processFreezerTick(stack, player);
                }
            }
        }
    }

    public static void processFreezerTick(ItemStack freezerStack, Player player) {
        var energyCap = freezerStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energyCap == null) return;

        int required = PortablefreezerItem.ENERGY_PER_TICK * 20;
        boolean wasActive = freezerStack.getOrCreateTag().getBoolean("Active");

        if (energyCap.getEnergyStored() >= required) {
            energyCap.extractEnergy(required, false);
            freezerStack.getOrCreateTag().putBoolean("Active", true);
            applyRefrigeration(freezerStack);
        } else {
            freezerStack.getOrCreateTag().putBoolean("Active", false);
            removeRefrigeration(freezerStack);
        }

        boolean isActive = freezerStack.getOrCreateTag().getBoolean("Active");
        if (wasActive != isActive) {
            if (player instanceof ServerPlayer sp) {
                TfcfreezerPacketHandler.send(PacketDistributor.PLAYER.with(() -> sp),
                        new SyncPortableFreezerPacket(freezerStack));
            }
        }
    }

    public static void forceRemoveRefrigeration(ItemStack freezerStack) {
        ItemStackHandler handler = getInventoryHandler(freezerStack);
        boolean changed = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack food = handler.getStackInSlot(slot);
            if (!food.isEmpty() && FoodCapability.hasTrait(food, TfcfreezerFoodTraits.freezing)) {
                handler.setStackInSlot(slot, FoodCapability.removeTrait(food.copy(), TfcfreezerFoodTraits.freezing));
                changed = true;
            }
        }
        if (changed) {
            freezerStack.getOrCreateTag().put("Inventory", handler.serializeNBT());
        }
        freezerStack.getOrCreateTag().putBoolean("Active", false);
    }

    private static void applyRefrigeration(ItemStack freezerStack) {
        ItemStackHandler handler = getInventoryHandler(freezerStack);
        boolean changed = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack food = handler.getStackInSlot(slot);
            if (!food.isEmpty() && !FoodCapability.hasTrait(food, TfcfreezerFoodTraits.freezing)) {
                handler.setStackInSlot(slot, FoodCapability.applyTrait(food.copy(), TfcfreezerFoodTraits.freezing));
                changed = true;
            }
        }
        if (changed) {
            freezerStack.getOrCreateTag().put("Inventory", handler.serializeNBT());
        }
    }

    private static void removeRefrigeration(ItemStack freezerStack) {
        ItemStackHandler handler = getInventoryHandler(freezerStack);
        boolean changed = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack food = handler.getStackInSlot(slot);
            if (!food.isEmpty() && FoodCapability.hasTrait(food, TfcfreezerFoodTraits.freezing)) {
                handler.setStackInSlot(slot, FoodCapability.removeTrait(food.copy(), TfcfreezerFoodTraits.freezing));
                changed = true;
            }
        }
        if (changed) {
            freezerStack.getOrCreateTag().put("Inventory", handler.serializeNBT());
        }
    }

    private static ItemStackHandler getInventoryHandler(ItemStack freezerStack) {
        ItemStackHandler handler = new ItemStackHandler(PortableFreezerContainer.SLOTS);
        CompoundTag tag = freezerStack.getOrCreateTag().getCompound("Inventory");
        if (!tag.isEmpty()) {
            handler.deserializeNBT(tag);
        }
        return handler;
    }
}