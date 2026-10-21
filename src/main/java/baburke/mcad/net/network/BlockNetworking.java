package baburke.mcad.net.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
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

                            // permission checks
                            // Check if player has person to modify each position
                            // Check whether the packet rate is reasonable to prevent spam.

                            // if placing play sound
                            if (state != Blocks.AIR.defaultBlockState()) {
                                SoundType soundType = state.getSoundType();
                                SoundEvent sound = soundType.getPlaceSound();
                                level.playSound(null, pos, sound, SoundSource.BLOCKS);
                            } else {
                                // if destroying play break sound of current block
                                BlockState currBlock = level.getBlockState(pos);

                                SoundEvent sound = currBlock.getSoundType().getBreakSound();
                                level.playSound(null, pos, sound, SoundSource.BLOCKS);
                            }

                            // Apply the block update.
                            level.setBlock(pos, state, 3);
                        }
                    });
                }
        );
    }
}