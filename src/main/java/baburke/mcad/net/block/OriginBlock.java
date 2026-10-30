package baburke.mcad.net.block;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class OriginBlock extends HorizontalDirectionalBlock {

    public OriginBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    public static BlockPos nearestOriginBlock(ClientLevel level,
                                              BlockPos center,
                                              int radius
    ) {
        BlockPos nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockState state = level.getBlockState(pos);

            if (state.is(ModBlocks.ORIGIN_BLOCK)) {
                double distance = pos.distSqr(center);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = pos.immutable();
                }
            }
        }

        return nearest;
    }
}
