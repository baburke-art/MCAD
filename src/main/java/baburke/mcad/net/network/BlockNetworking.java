package baburke.mcad.net.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class BlockNetworking {

    private static final double MAX_DISTANCE_SQUARED = 100 * 100;
    private static final int MAX_BLOCKS_PER_PACKET = 500;

    private BlockNetworking() {}

    public static void register() {
        // Register the serverbound payload
        PayloadTypeRegistry.serverboundPlay().register(
                UpdateBlocks.TYPE,
                UpdateBlocks.CODEC
        );

        // Register the receiver
        ServerPlayNetworking.registerGlobalReceiver(
                UpdateBlocks.TYPE,
                (payload, context) -> {
                    var player = context.player();

                    // Copy the payload data before scheduling world changes.
                    Map<BlockPos, BlockState> blocks = new HashMap<>(payload.blocks());

                    context.server().execute(() -> {
                        Level level = player.level();

                        // Enforce the packet block limit.
                        if (blocks.size() > MAX_BLOCKS_PER_PACKET) {
                            return;
                        }

                        for (var entry : blocks.entrySet()) {
                            BlockPos pos = entry.getKey();
                            BlockState state = entry.getValue();

                            // Don't modify unloaded positions.
                            if (!level.isLoaded(pos)) {
                                continue;
                            }

                            // Check if player has person to modify each position
                            // Check whether the packet rate is reasonable to prevent spam.

                            // Apply the block update.
                            level.setBlock(pos, state, 3);
                        }
                    });
                }
        );
    }
}