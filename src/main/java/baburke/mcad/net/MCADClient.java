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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MCADClient implements ClientModInitializer {

    private static final int MAX_LENGTH = 200;

    private static final net.minecraft.core.Direction NORTH = net.minecraft.core.Direction.NORTH;
    private static final net.minecraft.core.Direction SOUTH = net.minecraft.core.Direction.SOUTH;
    private static final net.minecraft.core.Direction EAST = net.minecraft.core.Direction.EAST;
    private static final net.minecraft.core.Direction WEST = net.minecraft.core.Direction.WEST;


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

                if (client.player == null) {continue;}

                if (client.gui.screen() instanceof MCADEditor) {
                    client.gui.setScreen(null); // Close
                } else {
                    client.gui.setScreen(new MCADEditor()); // Open
                }
            }
        });
    }

    public static void execute(
            //String name,
            Mode mode,
            String sizeInput,
            Direction direction,
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
        System.out.println("Direction: " + direction);
        System.out.println("length: " + length);
        System.out.println("Reversed: " + reversed);
        System.out.println("Interval: " + interval);

        Minecraft client = Minecraft.getInstance();
        BlockPos playerPos = client.player.blockPosition();
        ClientLevel level = client.level;

        // try to find an origin block within max length of player
        BlockPos origin = OriginBlock.nearestOriginBlock(level, playerPos, MAX_LENGTH);

        if (origin == null) {return;}

        // the start and end blocks of the sketch plane
//        BlockPos startBlock = new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ() + 1);
//        BlockPos endBlock = switch (direction) {
//            case X -> {
//                yield startBlock.offset(0, size - 1, size - 1);
//            }
//            case Y -> {
//                yield startBlock.offset(size - 1, 0, size - 1);
//            }
//            case Z -> {
//                yield startBlock.offset(size - 1, size - 1, 0);
//            }
//        };



        // Find active blocks next to origin block

        Map<BlockPos, BlockState> activeBlocks =
                new Sketch().findActiveBlocks(level, origin, size, direction);

        net.minecraft.core.Direction originFacing = level.getBlockState(origin).getValue(BlockStateProperties.HORIZONTAL_FACING);


        // For each block in map
        for (Map.Entry<BlockPos, BlockState> entry : activeBlocks.entrySet()) {
            // Map of the blocks along the full length of one active block at a time
            Map<BlockPos, BlockState> nextBlocks = getNextBlocks(originFacing, mode, direction, length, reversed, interval, entry);

            if (mode == Mode.EXTRUDE) {
                ClientPlayNetworking.send(
                        UpdateBlocks.place(nextBlocks)
                );
            } else if (mode == Mode.CUT) {
                ClientPlayNetworking.send(
                        UpdateBlocks.destroy(nextBlocks.keySet())
                );
            }
        }
    }

    private static @NonNull Map<BlockPos, BlockState> getNextBlocks(net.minecraft.core.Direction originFacing, Mode mode, Direction direction, int length, boolean reversed, int interval, Map.Entry<BlockPos, BlockState> entry) {
        BlockPos pos = entry.getKey();
        BlockState state = entry.getValue();

        Map<BlockPos, BlockState> blockLine = new HashMap<>();

        // Add active block
        blockLine.put(pos, state);

        // next line in block
        BlockPos nextPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

        int step = reversed ? -1 : 1;

        // counting up to next interval
        // Start at -1 so first block not counted
        int skipCount = -1;

        for (int i = 0; i < length; i++) {
            int offset = i * step;

            switch (direction) {
                case X:
                    // Origin block's X texture is facing
                    if (originFacing == NORTH) {
                        nextPos = pos.offset(0, 0, offset);
                    } else if (originFacing == SOUTH) {
                        nextPos = pos.offset(0, 0, -offset);
                    } else if (originFacing == EAST) {
                        nextPos = pos.offset(-offset, 0, 0);
                    } else if (originFacing == WEST) {
                        nextPos = pos.offset(offset, 0, 0);
                    }

                    break;
                case Y:
                    nextPos = pos.offset(0, offset, 0);
                    break;
                case Z:
                     // Origin block's X texture is facing
                    if (originFacing == NORTH) {
                        nextPos = pos.offset(-offset, 0, 0);
                    } else if (originFacing == SOUTH) {
                        nextPos = pos.offset( offset, 0,0);
                    } else if (originFacing == EAST) {
                        nextPos = pos.offset(0, 0, -offset);
                    } else if (originFacing == WEST) {
                        nextPos = pos.offset(0, 0, offset);
                    }
                    break;
            };

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