package com.github.Mev10.common.container;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.common.capabilities.PortableFreezerEnergyStorage;
import com.github.Mev10.common.event.PortableFreezerTickHandler;
import com.github.Mev10.common.item.PortablefreezerItem;
import com.github.Mev10.common.item.TfcfreezerFoodTraits;
import com.github.Mev10.network.SyncPortableFreezerPacket;
import com.github.Mev10.network.TfcfreezerPacketHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class PortableFreezerContainer extends AbstractContainerMenu {
    public static final int SLOTS = 18;
    public static final int CAPACITY = PortablefreezerItem.CAPACITY;
    public static final int ENERGY_PER_TICK = PortablefreezerItem.ENERGY_PER_TICK;

    private final ItemStack freezerStack;
    private final Player player;
    private final PortableFreezerEnergyStorage energyStorage;
    private final ItemStackHandler itemHandler;

    private boolean isTurnedOn;
    private boolean isActive;

    private boolean clientIsTurnedOn;
    private boolean clientIsActive;
    private int clientEnergy;

    private int tickCounter = 0;
    private static final int SYNC_INTERVAL = 20;

    public PortableFreezerContainer(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(windowId, playerInv, extraData.readItem());
    }

    public PortableFreezerContainer(int windowId, Inventory playerInv, ItemStack stack) {
        super(TfcfreezerContainers.PORTABLE_FREEZER_CONTAINER.get(), windowId);
        this.freezerStack = stack;
        this.player = playerInv.player;

        CompoundTag tag = stack.getOrCreateTag();
        this.isTurnedOn = tag.getBoolean("TurnedOn");
        this.isActive = tag.getBoolean("Active");

        this.clientIsTurnedOn = this.isTurnedOn;
        this.clientIsActive = this.isActive;
        this.clientEnergy = tag.getInt("Energy");

        this.energyStorage = new PortableFreezerEnergyStorage(stack);

        this.itemHandler = new ItemStackHandler(SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                saveToNBT();
                syncToClient();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return FoodCapability.get(stack) != null;
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) return stack;
                ItemStack result = super.insertItem(slot, stack, simulate);
                if (!simulate && canRefrigerate()) {
                    FoodCapability.applyTrait(result, TfcfreezerFoodTraits.freezing);
                }
                return result;
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (canRefrigerate()) return ItemStack.EMPTY;
                ItemStack extracted = super.extractItem(slot, amount, simulate);
                if (!simulate && !extracted.isEmpty()) {
                    FoodCapability.removeTrait(extracted, TfcfreezerFoodTraits.freezing);
                }
                return extracted;
            }
        };

        CompoundTag invTag = tag.getCompound("Inventory");
        if (!invTag.isEmpty()) itemHandler.deserializeNBT(invTag);

        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new SlotItemHandler(itemHandler, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return !canRefrigerate() && super.mayPickup(player);
                    }
                });

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 66 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 124));

        updateRefrigerationState();
        if (!player.level().isClientSide) syncToClient();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!player.level().isClientSide) {
            CompoundTag tag = freezerStack.getOrCreateTag();
            boolean newActive = tag.getBoolean("Active");
            if (this.isActive != newActive) {
                this.isActive = newActive;
                updateRefrigerationState();
            }
            this.isTurnedOn = tag.getBoolean("TurnedOn");

            tickCounter++;
            if (tickCounter % SYNC_INTERVAL == 0) {
                syncToClient();
            }
        }
    }

    private void updateRefrigerationState() {
        if (isActive) applyFreezerTraitToAllSlots();
        else removeFreezerTraitFromAllSlots();
    }

    public boolean canRefrigerate() {
        return isTurnedOn && isActive;
    }

    public void togglePower() {
        isTurnedOn = !isTurnedOn;
        if (!isTurnedOn) {
            isActive = false;
            freezerStack.getOrCreateTag().putBoolean("TurnedOn", false);
            freezerStack.getOrCreateTag().putBoolean("Active", false);
            // 立即移除所有物品的制冷特性
            for (int i = 0; i < SLOTS; i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ItemStack cleaned = FoodCapability.removeTrait(stack.copy(), TfcfreezerFoodTraits.freezing);
                    itemHandler.setStackInSlot(i, cleaned);
                }
            }
            saveToNBT();
        } else {
            isActive = true;
            freezerStack.getOrCreateTag().putBoolean("TurnedOn", true);
            freezerStack.getOrCreateTag().putBoolean("Active", true);
            updateRefrigerationState();
            saveToNBT();
        }
        persistToActualStack();
        syncToClient();
    }

    private void applyFreezerTraitToAllSlots() {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && !FoodCapability.hasTrait(stack, TfcfreezerFoodTraits.freezing)) {
                itemHandler.setStackInSlot(i, FoodCapability.applyTrait(stack.copy(), TfcfreezerFoodTraits.freezing));
            }
        }
    }

    private void removeFreezerTraitFromAllSlots() {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemHandler.setStackInSlot(i, FoodCapability.removeTrait(stack.copy(), TfcfreezerFoodTraits.freezing));
            }
        }
    }

    private void saveToNBT() {
        CompoundTag tag = freezerStack.getOrCreateTag();
        tag.putBoolean("TurnedOn", isTurnedOn);
        tag.putBoolean("Active", isActive);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    private void syncToClient() {
        if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
            TfcfreezerPacketHandler.send(PacketDistributor.PLAYER.with(() -> sp),
                    new SyncPortableFreezerPacket(freezerStack));
        }
    }

    private void persistToActualStack() {
        if (player.level().isClientSide) return;
        ItemStack actualStack = findMatchingFreezerInInventory(player);
        if (actualStack != null) {
            actualStack.setTag(freezerStack.getOrCreateTag().copy());
        }
    }

    public void receiveSyncData(CompoundTag tag) {
        this.clientIsTurnedOn = tag.getBoolean("TurnedOn");
        this.clientIsActive = tag.getBoolean("Active");
        this.clientEnergy = tag.getInt("Energy");
        CompoundTag invTag = tag.getCompound("Inventory");
        if (!invTag.isEmpty()) itemHandler.deserializeNBT(invTag);
    }

    public boolean isTurnedOn() {
        return player.level().isClientSide ? clientIsTurnedOn : isTurnedOn;
    }

    public int getEnergy() {
        return player.level().isClientSide ? clientEnergy : energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return CAPACITY;
    }

    public int getEnergyStoredScaled(int pixels) {
        if (getMaxEnergy() == 0) return 0;
        return (int) ((float) getEnergy() / getMaxEnergy() * pixels);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        if (index < SLOTS && canRefrigerate()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (index < SLOTS) {
            if (!moveItemStackTo(original, SLOTS, SLOTS + 36, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(original, 0, SLOTS, false)) return ItemStack.EMPTY;
        }

        if (original.getCount() == 0) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, original);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == freezerStack || player.getOffhandItem() == freezerStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveToNBT();
            persistToActualStack();
        }
    }

    private ItemStack findMatchingFreezerInInventory(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof PortablefreezerItem) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof PortablefreezerItem) return offHand;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PortablefreezerItem) return stack;
        }
        return null;
    }
}
