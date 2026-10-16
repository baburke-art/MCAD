package baburke.mcad.net.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BlockNetworking {

    private BlockNetworking() {}

    public static void register() {
        // Register the payload type
        PayloadTypeRegistry.serverboundPlay().register(
                UpdateBlock.TYPE,
                UpdateBlock.CODEC
        );

        // Register the server receiver
        ServerPlayNetworking.registerGlobalReceiver(
                UpdateBlock.TYPE,
                (payload, context) -> {
                    var player = context.player();

                    var pos = payload.pos();
                    var state = payload.state();

                    var level = player.level();

                    // Only allow changes within 48 blocks
                    if (player.blockPosition().distSqr(pos) > 2304) {
                        return;
                    }

                    // Don't modify unloaded positions
                    if (!level.isLoaded(pos)) {
                        return;
                    }

                    // Destroy or place/replace the block
                    //level.destroyBlock(pos, true, player);

                    // Place block
                    level.setBlock(pos, state, 3);
                }
        );
    }
}