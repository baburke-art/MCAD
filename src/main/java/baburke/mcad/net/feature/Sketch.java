package baburke.mcad.net.feature;

import baburke.mcad.net.network.UpdateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

// Maps active blocks in a rectangle next to the origin block.

public class Sketch {

    public Map<BlockPos, BlockState> findBlocks(
            ClientLevel level,
            BlockPos origin,
            int size) {

        Map<BlockPos, BlockState> blocks = new HashMap<>();

        BlockPos xPlanePos = origin.offset(0, 0, 1);

        for (BlockPos pos : BlockPos.betweenClosed(
                xPlanePos,
                // (Fixed vertical) Will need to also change with orientation
                xPlanePos.offset(0, size-1, size-1))) {

            BlockState state = level.getBlockState(pos);

            ClientPlayNetworking.send(
                    new UpdateBlock(pos.immutable(), Blocks.STONE.defaultBlockState())
            );

//            for (int i = 0; i < size; i++) {
//                BlockPos newPos = new  BlockPos(pos.getX(), pos.getY(), pos.getZ() + i);
//
//                ClientPlayNetworking.send(
//                        new UpdateBlock(newPos, Blocks.STONE.defaultBlockState())
//                );
//            }



            // If not air
            if (!state.isAir()) {
                blocks.put(pos.immutable(), state);
            }
        }

        return blocks;
    }
}