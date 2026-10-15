package baburke.mcad.net.item;

import baburke.mcad.net.MCAD;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModItems {

    public static final Item FLUORITE = registerItem("fluorite", Item::new);

    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        Identifier id = Identifier.fromNamespaceAndPath(MCAD.MOD_ID, name);

        return Registry.register(BuiltInRegistries.ITEM, id, function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    }

    public static void registerModItems() {
        MCAD.LOGGER.info("Registering Mod Items for " + MCAD.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(FLUORITE);
        });

    }
}
