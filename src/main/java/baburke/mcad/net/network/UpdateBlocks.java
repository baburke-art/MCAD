package baburke.mcad.net.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public record UpdateBlocks(Map<BlockPos, BlockState> blocks)
        implements CustomPacketPayload {

    public static final Type<UpdateBlocks> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mcad", "update_blocks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBlocks> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            java.util.HashMap::new,
                            BlockPos.STREAM_CODEC,
                            ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC)
                    ),
                    UpdateBlocks::blocks,
                    UpdateBlocks::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Destroy blocks
    public static UpdateBlocks destroy(Iterable<BlockPos> positions) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();

        for (BlockPos pos : positions) {
            blocks.put(pos, Blocks.AIR.defaultBlockState());
        }

        return new UpdateBlocks(blocks);
    }

    // Place blocks
    public static UpdateBlocks place(Map<BlockPos, BlockState> blocks) {
        return new UpdateBlocks(new HashMap<>(blocks));
    }
}