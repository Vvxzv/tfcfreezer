package com.github.Mev10.common.blockentities;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.common.capabilities.DelegateEnergyStorage;
import com.github.Mev10.common.capabilities.EnergyStorageCallback;
import com.github.Mev10.common.capabilities.InventoryConsumerEnergyStorage;
import com.github.Mev10.common.container.freezerContainer;
import com.github.Mev10.common.item.TfcfreezerFoodTraits;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class freezerBlockEntity extends ApplianceBlockEntity<freezerBlockEntity.freezerInventory> implements EnergyStorageCallback {
    public static final int SLOTS = 27;
    private static final Component NAME = Component.translatable(String.format("block.%s.freezer", Tfcfreezer.MOD_ID));

    private boolean prevRefrigerationState = false; // 记录前一次制冷状态

    public freezerBlockEntity(BlockPos pos, BlockState state) {
        super(TfcfreezerBlocksEntities.freezer_BLOCK.get(), pos, state, freezerInventory::new, NAME,true,40);

        // 初始设置提取规则
        updateExtractionRules();
    }

    // 添加：根据制冷状态更新物品提取规则
    private void updateExtractionRules() {
        if (canRefrigerate()) {
            // 制冷时禁止所有方向提取
            sidedInventory.on(new PartialItemHandler(inventory).extractAll(), d -> false);
        } else {
            // 非制冷时允许下方提取
            sidedInventory.on(new PartialItemHandler(inventory).extractAll(), d -> d == Direction.DOWN);
            sidedInventory.on(new PartialItemHandler(inventory).insertAll(), d -> d != Direction.DOWN);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, freezerBlockEntity freezer)
    {
        freezer.checkForLastTickSync();
        if (level.getGameTime() % 2 == 0)
        {
            boolean currentState = freezer.canRefrigerate();

            if(freezer.canRefrigerate()){
                freezer.consumeEnergyForTicks(2);
                if(freezer.inventory.energyStorage.getEnergyStored() < freezer.energyTickConsumption){
                    freezer.setActivity(false);
                    freezer.removefreezerTraitFromInventory();
                }
            }
            else if(freezer.isTurnedOn() && !freezer.isActive()){
                if(freezer.inventory.energyStorage.getEnergyStored() >= freezer.energyTickConsumption){
                    freezer.setActivity(true);
                    freezer.applyfreezerTraitToInventory();
                    freezer.consumeEnergyForTicks(2);
                }
            }

            // 检查状态是否变化，如果变化则更新提取规则
            if (freezer.prevRefrigerationState != currentState) {
                freezer.updateExtractionRules();
                freezer.prevRefrigerationState = currentState;
            }

            freezer.markForSync();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory inv, Player player) {
        return freezerContainer.create(this, inv, windowID);
    }

    public boolean canRefrigerate()
    {
        return isTurnedOn() && isActive();
    }

    @Override
    public void toggleAppliance() {
        if(isTurnedOn()){
            turnOff();
        }else{
            turnOn();
        }
        markForSync();
    }

    @Override
    public void ejectInventory() {
        turnOff();
        super.ejectInventory();
    }

    @Override
    public void energyLevelChanged()
    {
    }

    @Override
    protected void turnOff() {
        super.turnOff();
        removefreezerTraitFromInventory();
    }

    @Override
    protected void turnOn() {
        super.turnOn();
        if(canRefrigerate()){
            applyfreezerTraitToInventory();
        }
    }

    private void applyfreezerTraitToInventory(){
        inventory.applyfreezerTraitToInventory();
    }

    private void removefreezerTraitFromInventory(){
        inventory.removefreezerTraitFromInventory();
    }

    public static class freezerInventory extends InventoryItemHandler implements DelegateEnergyStorage, INBTSerializable<CompoundTag> {
        private static final int CAPACITY = 100000;
        private static final int MAX_TRANSFER = 800;

        private final freezerBlockEntity freezer;
        private final InventoryConsumerEnergyStorage energyStorage;

        public freezerInventory(InventoryBlockEntity<freezerInventory> entity)
        {
            super(entity, SLOTS);
            freezer = (freezerBlockEntity) entity;
            energyStorage = new InventoryConsumerEnergyStorage(CAPACITY, MAX_TRANSFER, (EnergyStorageCallback) entity);
        }

        @Deprecated
        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            if(freezer.canRefrigerate() && !FoodCapability.hasTrait(stack, TfcfreezerFoodTraits.freezing)){
                super.setStackInSlot(slot, FoodCapability.applyTrait(stack.copy(), TfcfreezerFoodTraits.freezing));
            }else{
                super.setStackInSlot(slot, stack.copy());
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if(freezer.canRefrigerate() && !FoodCapability.hasTrait(stack, TfcfreezerFoodTraits.freezing)){
                return super.insertItem(slot, FoodCapability.applyTrait(stack.copy(), TfcfreezerFoodTraits.freezing), simulate);
            }else{
                return  super.insertItem(slot, stack.copy(), simulate);
            }
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // 制冷状态下禁止物品取出
            if (freezer.canRefrigerate()) {
                return ItemStack.EMPTY;
            }

            // 非制冷状态正常取出
            return FoodCapability.removeTrait(super.extractItem(slot, amount, simulate).copy(), TfcfreezerFoodTraits.freezing);
        }

        public void applyfreezerTraitToInventory(){
            for (int i = 0; i < SLOTS; i++)
            {
                super.setStackInSlot(i, FoodCapability.applyTrait(super.getStackInSlot(i).copy(), TfcfreezerFoodTraits.freezing));
            }
        }

        public void removefreezerTraitFromInventory(){
            for (int i = 0; i < SLOTS; i++)
            {
                super.setStackInSlot(i, FoodCapability.removeTrait(super.getStackInSlot(i).copy(), TfcfreezerFoodTraits.freezing));
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFoodStack(stack) && super.isItemValid(slot, stack);
        }

        @Override
        public IEnergyStorage getEnergyStorage() {
            return energyStorage;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compoundTag = super.serializeNBT();
            compoundTag.put("EnergyStorage", energyStorage.serializeNBT());
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);
            energyStorage.deserializeNBT(nbt.get("EnergyStorage"));
        }

        public boolean isFoodStack(ItemStack stack){
            return !stack.isEmpty() && FoodCapability.get(stack) != null;
        }
    }
}
