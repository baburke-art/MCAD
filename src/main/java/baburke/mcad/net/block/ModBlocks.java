package baburke.mcad.net.block;

import baburke.mcad.net.MCAD;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {

    public static final Block ORIGIN_BLOCK =  registerBlock(
            "origin_block",
            properties -> new Block(
                    properties
                            .strength(0.5F)
                            .sound(SoundType.TRIAL_SPAWNER)
                            .lightLevel(state -> 15)
                            .noOcclusion()
            )
    );

    public static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MCAD.MOD_ID, name))));

        registerBlockItem(name, toRegister);

        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MCAD.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(MCAD.MOD_ID, name);

        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block,
                        new Item.Properties().useBlockDescriptionPrefix()
                                .setId(ResourceKey.create(Registries.ITEM, id))));
    }

    public static void registerModBlocks() {
        MCAD.LOGGER.info("Registering Mod Blocks for " + MCAD.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(ORIGIN_BLOCK);
        });
    }

}
