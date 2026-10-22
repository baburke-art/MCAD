package baburke.mcad.net.feature;

import baburke.mcad.net.network.UpdateBlocks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

// Maps active blocks in a rectangle next to the origin block.

public class Sketch {

    public Map<BlockPos, BlockState> findActiveBlocks(
            ClientLevel level,
            BlockPos origin,
            int size,
            BlockPos startBlock,
            BlockPos endBlock) {

        Map<BlockPos, BlockState> blocks = new HashMap<>();

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