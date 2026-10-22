package baburke.mcad.net.feature;

import baburke.mcad.net.Direction;
import baburke.mcad.net.network.UpdateBlocks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.Map;

// Maps active blocks in a rectangle next to the origin block.

public class Sketch {

    public Map<BlockPos, BlockState> findActiveBlocks(
            ClientLevel level,
            BlockPos origin,
            int size,
            Direction direction
    ) {

        Map<BlockPos, BlockState> blocks = new HashMap<>();

        BlockPos startBlock = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
        BlockPos endBlock = new BlockPos(origin.getX(), origin.getY(), origin.getZ());



        net.minecraft.core.Direction originFace = level.getBlockState(origin).getValue(BlockStateProperties.HORIZONTAL_FACING);

        System.out.println("origin facing: " + originFace);

        net.minecraft.core.Direction NORTH = net.minecraft.core.Direction.NORTH;
        net.minecraft.core.Direction SOUTH = net.minecraft.core.Direction.SOUTH;
        net.minecraft.core.Direction EAST = net.minecraft.core.Direction.EAST;
        net.minecraft.core.Direction WEST = net.minecraft.core.Direction.WEST;

        // Neg z = North
        // positive z = South
        // positive x = east
        // negative x = west

        // positive x is current code correct with origin facing west (the opposite)
        // Just adjust the opposite facing to start.


        switch (originFace) {
            case NORTH:
                startBlock = new BlockPos(origin.getX() - 1, origin.getY(), origin.getZ() + 1);

                endBlock = switch (direction) {
                    case X -> {
                        yield startBlock.offset(-size + 1, size - 1, 0);
                    }
                    case Y -> {
                        yield startBlock.offset(-size + 1, 0, size - 1);
                    }
                    case Z -> {
                        yield startBlock.offset(0, size - 1, size - 1);
                    }
                };

                break;
            case SOUTH:
                startBlock = new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ() - 1);

                endBlock = switch (direction) {
                    case X -> {
                        yield startBlock.offset(size - 1, size - 1, 0);
                    }
                    case Y -> {
                        yield startBlock.offset(size - 1, 0, -size + 1);
                    }
                    case Z -> {
                        yield startBlock.offset(0, size - 1, -size + 1);
                    }
                };

                break;
            case EAST:
                startBlock = new BlockPos(origin.getX() - 1, origin.getY(), origin.getZ() - 1);

                endBlock = switch (direction) {
                    case X -> {
                        yield startBlock.offset(0, size - 1, -size + 1);
                    }
                    case Y -> {
                        yield startBlock.offset(-size + 1, 0, -size + 1);
                    }
                    case Z -> {
                        yield startBlock.offset(-size + 1, size - 1, 0);
                    }
                };

                // Origin facing East is working but extrude length needs to be reversed in getNextBlocks().


                break;
            case WEST:
                startBlock = new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ() + 1);

                endBlock = switch (direction) {
                    case X -> {
                        yield startBlock.offset(0, size - 1, size - 1);
                    }
                    case Y -> {
                        yield startBlock.offset(size - 1, 0, size - 1);
                    }
                    case Z -> {
                        yield startBlock.offset(size - 1, size - 1, 0);
                    }
                };

                break;


        }

        //BlockPos testPos = startBlock.offset(0, -size + 1, -size + 1);

        //System.out.println("test pos: " + testPos);

//        ClientPlayNetworking.send(
//                UpdateBlocks.place(startBlock.offset(0, -size + 1, -size + 1).immutable(), Blocks.BAMBOO_PLANKS.defaultBlockState())
//        );

        for (BlockPos pos : BlockPos.betweenClosed(
                startBlock,
                endBlock
        )) {

            BlockState state = level.getBlockState(pos);

            // If not air
            if (!state.isAir()) {
                blocks.put(pos.immutable(), state);
            }

            // For Visual Guide
            // update when size or direction changes.
            // if pos.state == Blocks.Air skip it
            for (int i = 0; i < size; i++) {
                ClientPlayNetworking.send(
                        UpdateBlocks.place(pos.immutable(), Blocks.STONE.defaultBlockState())
                );
            }


        }

        return blocks;
    }

}