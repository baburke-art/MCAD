package baburke.mcad.net;

import baburke.mcad.net.gui.MCADEditor;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class MCADClient implements ClientModInitializer {

    // Defines Keybind category
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            "mcad", "general"
                    )
            );

    // Adds MCAD Editor keybind (Default "J")
    private static final KeyMapping OPEN_FORM =
            KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "Open/Close Editor",
                            InputConstants.KEY_J,
                            CATEGORY
                    )
            );

    @Override
    public void onInitializeClient() {

        // Open/Close MCAD Editor GUI
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_FORM.consumeClick()) {
                if (client.player != null) {
                    if (client.gui.screen() instanceof MCADEditor) {
                        client.gui.setScreen(null);
                    } else {
                        client.gui.setScreen(new MCADEditor());
                    }
                }
            }
        });
    }
}