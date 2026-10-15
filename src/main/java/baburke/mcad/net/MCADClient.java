package baburke.mcad.net;

import baburke.mcad.net.gui.FormScreen;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class MCADClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            "mcad", "general"
                    )
            );

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_FORM.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new FormScreen());
                }
            }
        });
    }
}