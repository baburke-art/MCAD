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
import java.util.LinkedHashMap;
import java.util.Map;

public record UpdateBlocks(Map<BlockPos, BlockState> blocks)
        implements CustomPacketPayload {

    public static final Type<UpdateBlocks> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mcad", "update_blocks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBlocks> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            java.util.LinkedHashMap::new,
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

    // Update blocks by destroying
    public static UpdateBlocks update(Map<BlockPos, BlockState> positions, BlockState state) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        for (BlockPos pos : positions.keySet()) {
            blocks.put(pos, state);
        }

        return new UpdateBlocks(blocks);
    }

    // Update blocks by placing
    public static UpdateBlocks update(Map<BlockPos, BlockState> blocks) {
        return new UpdateBlocks(new LinkedHashMap<>(blocks));
    }
}