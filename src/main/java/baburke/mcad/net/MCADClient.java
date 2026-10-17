package baburke.mcad.net;

import baburke.mcad.net.feature.Sketch;
import baburke.mcad.net.feature.Execute;
import baburke.mcad.net.feature.OriginBlock;
import baburke.mcad.net.gui.MCADEditor;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

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

    public void execute(int size, int length){

        Minecraft client = Minecraft.getInstance();

        BlockPos playerPos = client.player.blockPosition();
        ClientLevel level = client.level;

        // try to find an origin block within 50 blocks of player
        BlockPos pos = new OriginBlock().nearestOriginBlock(level, playerPos, 50);

        // If a nearby origin block is found
        if (pos != null) {
            System.out.println("Origin Block found!: " + pos);
            // Find active blocks next to origin block with size 10 rectangle
            // Add variable for orientation and
            Map<BlockPos, BlockState> blocks =
                    new Sketch().findBlocks(level, pos, size); // test size 10 blocks

            // Change to blocks list map instead of pos
            new Execute().extrude(client, blocks, length);
        }

    }
}