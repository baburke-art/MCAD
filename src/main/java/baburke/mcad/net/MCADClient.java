package baburke.mcad.net;

import baburke.mcad.net.feature.Sketch;
import baburke.mcad.net.block.OriginBlock;
import baburke.mcad.net.gui.MCADEditor;

import baburke.mcad.net.network.UpdateBlocks;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MCADClient implements ClientModInitializer {

    private static final Minecraft client = Minecraft.getInstance();

    private static final int MAX_LENGTH = 200;

    private static String originMessage = "";
    private static long expiryTick = -1;
    private static final long DISPLAY_TICKS = 120; // 3 seconds at 20 TPS

    // Defines Keybind category
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            "mcad", "general"
                    )
            );

    // Adds MCAD Editor keybind (Default "J")
    public static final KeyMapping OPEN_FORM =
            KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.mcad.open_form",
                            InputConstants.Type.KEYBOARD,
                            InputConstants.KEY_J,
                            CATEGORY
                    )
            );

    @Override
    public void onInitializeClient() {
        // Open/Close MCAD Editor GUI
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_FORM.consumeClick()) {

                if (client.player == null) {continue;}

                if (client.gui.screen() instanceof MCADEditor) {
                    client.gui.setScreen(null); // Close
                } else {
                    client.gui.setScreen(new MCADEditor()); // Open
                }
            }
        });

        // Origin found text id
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mymod", "origin_location_text"),
                (graphics, tickCounter) -> renderOriginText(graphics)
        );

        // Plane guide renderer
        PlaneGuideRenderer.register();

    }

    public static void execute(
            //String name,
            Mode mode,
            String sizeInput,
            PlaneDirection planeDirection,
            String lengthInput,
            boolean reversed,
            String intervalInput
    ){
        int size;
        int length;
        int interval;

        try {
            size = Integer.parseInt(sizeInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid size: " + sizeInput);
            return;
        }
        try {
            length = Integer.parseInt(lengthInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid length: " + lengthInput);
            return;
        }
        try {
            interval = Integer.parseInt(intervalInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid interval: " + intervalInput);
            return;
        }

        // Validate length and size
        if(length == 0 || size == 0) {return;}
        if (length < 0 || size < 0) {
            System.out.println("Invalid negative!");
            return;
        }
        if (length > MAX_LENGTH) {
            System.out.println("Max length is 100!");
            return;
        }
        if (size > MAX_LENGTH) {
            System.out.println("Max size is 100!");
            return;
        }

        if (interval > MAX_LENGTH || interval < 0) {interval = 0;}

        //MCADClient.execute(size, length, reversed);

        //System.out.println("Name: " + name);
        System.out.println("Mode: " + mode);
        System.out.println("Size: " + size);
        System.out.println("Direction: " + planeDirection);
        System.out.println("length: " + length);
        System.out.println("Reversed: " + reversed);
        System.out.println("Interval: " + interval);

        Minecraft client = Minecraft.getInstance();
        BlockPos playerPos = client.player.blockPosition();
        ClientLevel level = client.level;

        // try to find an origin block within max length of player
        BlockPos origin = OriginBlock.nearestOriginBlock(level, playerPos, MAX_LENGTH);

        if (origin == null) {return;}

        showOriginText(origin);

        Direction originFacing = level.getBlockState(origin).getValue(BlockStateProperties.HORIZONTAL_FACING);

        if (mode == Mode.FASTCUT) {
            // Find all blocks next to origin block
            Map<BlockPos, BlockState> allBlocks = new Sketch().getAllBlocks(level, origin, size, planeDirection);

            for (Map.Entry<BlockPos, BlockState> block : allBlocks.entrySet()) {
                Map<BlockPos, BlockState> blockLine = getNextBlocks(level, origin, originFacing, mode, planeDirection, length, reversed, interval, block);

                ClientPlayNetworking.send(
                        UpdateBlocks.destroy(blockLine.keySet())
                );
            }
            return;
        }

        // Find active blocks next to origin block
        Map<BlockPos, BlockState> activeBlocks = new Sketch().findActiveBlocks(level, origin, size, planeDirection);

        // For each block in map
        for (Map.Entry<BlockPos, BlockState> block : activeBlocks.entrySet()) {

            // Map of the blocks along the full length of one active block at a time
            Map<BlockPos, BlockState> blockLine = getNextBlocks(level, origin, originFacing, mode, planeDirection, length, reversed, interval, block);

            if (mode == Mode.EXTRUDE || mode == Mode.JOIN) {
                ClientPlayNetworking.send(
                        UpdateBlocks.place(blockLine)
                );
            } else if (mode == Mode.CUT || mode == Mode.MATCHCUT) {
                ClientPlayNetworking.send(
                        UpdateBlocks.destroy(blockLine.keySet())
                );
            }
        }
    }

    public static void renderOriginText(GuiGraphicsExtractor graphics) {
        if (expiryTick < 0) {
            return;
        }

        long currentTick = client.level.getGameTime();

        if (currentTick >= expiryTick) {
            return;
        }

        graphics.text(
                client.font,
                originMessage,
                100,
                200,
                0xFFFFFFFF,
                false
        );
    }

    private static void showOriginText(BlockPos pos) {
        originMessage = "Origin found at ("
                + pos.getX() + ", "
                + pos.getY() + ", "
                + pos.getZ() + ")";

        expiryTick = client.level.getGameTime() + 60;
    }

    private static @NonNull Map<BlockPos, BlockState> getNextBlocks(Level level,
                                                                    BlockPos origin,
                                                                    Direction originFacing,
                                                                    Mode mode,
                                                                    PlaneDirection planeDirection,
                                                                    int length,
                                                                    boolean reversed,
                                                                    int interval,
                                                                    Map.Entry<BlockPos, BlockState> blocks) {
        BlockPos pos = blocks.getKey();
        BlockState state = blocks.getValue();

        Map<BlockPos, BlockState> blockLine = new HashMap<>();

        // Add active block
        blockLine.put(pos, state);

        // next line in block
        BlockPos nextPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

        int step = reversed ? -1 : 1;

        // counting up to next interval
        // Start at -1 so active block not counted
        int skipCount = -1;

        for (int i = 0; i < length; i++) {
            int offset = i * step;

            BlockState currState = level.getBlockState(pos);

            switch (planeDirection) {
                // From current active block's position using local coordinates
                case A -> nextPos = Sketch.localOffset(pos, originFacing, 0, 0, offset);
                case B -> nextPos = Sketch.localOffset(pos, originFacing, -offset, 0, 0);
                case C -> nextPos = Sketch.localOffset(pos, originFacing, 0, offset, 0);

            };

            // Not counting the active block, If next block is not air and mode is join, end line and return it.
            if (i > 0 && !level.getBlockState(nextPos).isAir() && mode == Mode.JOIN) {return blockLine;}

            // If current block is not same type as next block skip it
            if (level.getBlockState(nextPos) != currState && mode == Mode.MATCHCUT) {continue;}

            // Place next block each time interval is reached
            if (interval == 0) {
                blockLine.put(nextPos, state);
            } else if (skipCount == interval) {
                blockLine.put(nextPos, state);
                skipCount = 0;
            } else {
                skipCount++;
            }

        }
        return blockLine;
    }


}