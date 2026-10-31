package baburke.mcad.net.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
                    Map<BlockPos, BlockState> blocks = new LinkedHashMap<>(payload.blocks());

                    context.server().execute(() -> {
                        Level level = player.level();

                        if (blocks.isEmpty()){return;}

                        // Enforce the packet block limit.
                        if (blocks.size() > MAX_BLOCKS_PER_PACKET) {
                            return;
                        }

                        // First block = the "active" block
                        BlockPos activeBlock = blocks.keySet().iterator().next();
                        // Store active blocks entity data first on server.
                        BlockEntity blockEntity = level.getBlockEntity(activeBlock);

                        CompoundTag tag;
                        BlockEntity copy = null;

                        // Save a copy because active block is deleted to register block updates like copper golem.
                        if (blockEntity != null) {
                            tag = blockEntity.saveWithoutMetadata(level.registryAccess());
                            copy = blockEntity.getType().create(activeBlock, blockEntity.getBlockState());

                            ValueInput input = TagValueInput.create(
                                    ProblemReporter.DISCARDING,
                                    level.registryAccess(),
                                    tag
                            );
                            copy.loadWithComponents(input);

                            blockEntity.setRemoved();
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

                            //System.out.println(pos  + " , " + state);

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

                            // Remove current block first
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                            // Apply the block update.
                            level.setBlock(pos, state, 3);

                            // update relevant block entity (chest with items, sign with writing, etc)
                            if (copy != null) {
                                if (copy instanceof ChestBlockEntity source
                                        && level.getBlockEntity(pos) instanceof ChestBlockEntity target) {
                                    int count = Math.min(source.getContainerSize(), target.getContainerSize());

                                    for (int i = 0; i < count; i++) {
                                        target.setItem(i, source.getItem(i).copy());
                                    }

                                    target.setChanged();
                                } else if (copy instanceof SignBlockEntity source
                                        && level.getBlockEntity(pos) instanceof SignBlockEntity target) {

                                    SignText front = source.getText(SignTextSlot.FRONT);
                                    SignText back = source.getText(SignTextSlot.BACK);

                                    target.setText(front, SignTextSlot.FRONT);
                                    target.setText(back, SignTextSlot.BACK);

                                    target.setChanged();
                                }

                                // to stop items in active block chests from popping out into items when active block destroyed.
                                blockEntity.clearRemoved();
                            }
                        }
                    });
                }
        );
    }
}