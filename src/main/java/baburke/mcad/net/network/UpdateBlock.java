package baburke.mcad.net.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

public record UpdateBlock(BlockPos pos, BlockState state)
        implements CustomPacketPayload {

    public static final Type<UpdateBlock> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mcad", "update_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBlock> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    UpdateBlock::pos,
                    ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC),
                    UpdateBlock::state,
                    UpdateBlock::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static UpdateBlock destroy(BlockPos pos) {
        return new UpdateBlock(pos, Blocks.AIR.defaultBlockState());
    }

    public static UpdateBlock place(BlockPos pos, BlockState state) {return new UpdateBlock(pos, state);}
}