package com.github.Mev10.common.event;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.common.container.PortableFreezerContainer;
import com.github.Mev10.common.item.PortablefreezerItem;
import com.github.Mev10.common.item.TfcfreezerFoodTraits;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(modid = Tfcfreezer.MOD_ID)
public class FreezerDropHandler {

    /**
     * 当玩家丢弃物品时触发（按 Q 或拖拽出背包）
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        processFreezerStack(stack);
    }

    /**
     * 当任何物品实体加入世界时触发（涵盖所有生成途径：丢弃、死亡掉落、漏斗、管道等）
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            processFreezerStack(stack);
        }
    }

    /**
     * 处理便携冰箱的物品堆：如果冰箱是开启状态，则关闭它并清除内部食物的制冷特性
     */
    private static void processFreezerStack(ItemStack stack) {
        if (!(stack.getItem() instanceof PortablefreezerItem)) return;
        if (!PortablefreezerItem.isTurnedOn(stack)) return;

        // 关闭冰箱
        PortablefreezerItem.setTurnedOn(stack, false);
        stack.getOrCreateTag().putBoolean("Active", false);

        // 清除内部食物的制冷特性
        CompoundTag invTag = stack.getOrCreateTag().getCompound("Inventory");
        if (!invTag.isEmpty()) {
            ItemStackHandler handler = new ItemStackHandler(PortableFreezerContainer.SLOTS);
            handler.deserializeNBT(invTag);
            boolean changed = false;
            for (int i = 0; i < PortableFreezerContainer.SLOTS; i++) {
                ItemStack food = handler.getStackInSlot(i);
                if (!food.isEmpty() && FoodCapability.hasTrait(food, TfcfreezerFoodTraits.freezing)) {
                    ItemStack cleaned = FoodCapability.removeTrait(food.copy(), TfcfreezerFoodTraits.freezing);
                    handler.setStackInSlot(i, cleaned);
                    changed = true;
                }
            }
            if (changed) {
                stack.getOrCreateTag().put("Inventory", handler.serializeNBT());
            }
        }
    }
}