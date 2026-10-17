package baburke.mcad.net.feature;

import baburke.mcad.net.block.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class OriginBlock {

    public BlockPos nearestOriginBlock(ClientLevel level,
                                              BlockPos center,
                                              int radius) {

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockState state = level.getBlockState(pos);

            if (state.is(ModBlocks.ORIGIN_BLOCK)) {
                return pos.immutable();
            }
        }

        return null;
    }
}
