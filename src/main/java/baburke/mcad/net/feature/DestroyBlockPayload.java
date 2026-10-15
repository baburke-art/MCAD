package baburke.mcad.net.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DestroyBlockPayload(BlockPos pos)
        implements CustomPacketPayload {

    public static final Type<DestroyBlockPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mcad", "destroy_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DestroyBlockPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    DestroyBlockPayload::pos,
                    DestroyBlockPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}