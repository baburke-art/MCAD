package baburke.mcad.net.feature;

import baburke.mcad.net.PlaneDirection;
import baburke.mcad.net.network.UpdateBlocks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Maps active blocks in a rectangle next to the origin block.

public class Sketch {

    public Map<BlockPos, BlockState> findActiveBlocks(
            ClientLevel level,
            BlockPos origin,
            int size,
            PlaneDirection planeDirection
    ) {

        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        Direction originFace = level.getBlockState(origin).getValue(BlockStateProperties.HORIZONTAL_FACING);

        // Local coordinate of starting block next to origin
        BlockPos startBlock = localOffset(origin, originFace, -1, 0, 1);

        BlockPos endBlock = switch (planeDirection) {
            case A -> localOffset(origin, originFace, -size, size - 1, 1);
            case C -> localOffset(origin, originFace, -size, 0, size);
            case B -> localOffset(origin, originFace, -1, size - 1, size);
        };

        // Neg z = North
        // positive z = South
        // positive x = east
        // negative x = west

        for (BlockPos pos : BlockPos.betweenClosed(
                startBlock,
                endBlock
        )) {
            BlockState state = level.getBlockState(pos);

            // If not air
            if (!state.isAir()) {
                blocks.put(pos.immutable(), state);
            }

            // For Visual Guide of plane
            // update when size or direction changes.
            // if pos.state == Blocks.Air skip it
//            for (int i = 0; i < size; i++) {
//                ClientPlayNetworking.send(
//                        UpdateBlocks.place(pos.immutable(), Blocks.STONE.defaultBlockState())
//                );
//            }
        }
        return blocks;
    }

    public static BlockPos localOffset(
            BlockPos pos,
            Direction facing,
            int x, int y, int z
    ) {
        Direction right = facing.getClockWise();
        Direction up = Direction.UP;
        Direction backward = facing.getOpposite();

        return pos
                .relative(right, x)
                .relative(up, y)
                .relative(backward, z);
    }

}