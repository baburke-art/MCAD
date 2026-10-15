package baburke.mcad.net;

import baburke.mcad.net.feature.DestroyBlockPayload;
import baburke.mcad.net.feature.Extrude;
import baburke.mcad.net.gui.FormScreen;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import net.minecraft.client.Minecraft;

public class MCADClient implements ClientModInitializer {

    private BlockPos lastSentPos = null;

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

        Extrude.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_FORM.consumeClick()) {
                if (client.player != null) {
                    if (client.gui.screen() instanceof FormScreen) {
                        client.gui.setScreen(null);
                    } else {
                        client.gui.setScreen(new FormScreen());
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                lastSentPos = null;
                return;
            }

            // Playing with sending a destroy block below player request to server.
            BlockPos pos = client.player.blockPosition().below();
            var state = client.level.getBlockState(pos);

            if (!state.isAir() && !pos.equals(lastSentPos)) {
//                ClientPlayNetworking.send(
//                        new DestroyBlockPayload(pos)
//                );

                lastSentPos = pos;
            }
        });
    }
}