package com.github.Mev10.common.block;

import com.github.Mev10.Tfcfreezer;
import com.github.Mev10.common.blockentities.freezerBlockEntity;
import com.github.Mev10.common.blockentities.TfcfreezerBlocksEntities;
import com.github.Mev10.common.item.TfcfreezerItems;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class TfcfreezerBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Tfcfreezer.MOD_ID);

    public static final RegistryObject<Block> freezer_BLOCK = register("freezer",
            () -> new freezerBlock(ExtendedProperties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .blockEntity(TfcfreezerBlocksEntities.freezer_BLOCK)
                    .serverTicks(freezerBlockEntity::serverTick)
                    .clientTicks(freezerBlockEntity::clientTick)
            ));

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier) {
        return register(name, blockSupplier, block -> new BlockItem(block, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory) {
        return RegistrationHelpers.registerBlock(BLOCKS, TfcfreezerItems.ITEMS, name, blockSupplier, blockItemFactory);
    }
}

