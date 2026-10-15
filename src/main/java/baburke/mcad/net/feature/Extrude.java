package baburke.mcad.net.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class Extrude {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player != null && mc.level != null) {
                BlockPos pos = mc.player.blockPosition().below();
                BlockState state = mc.level.getBlockState(pos);

                // Send block pos to server to be destroyed instead
                //mc.level.destroyBlock(pos, true, mc.player);
            }
        });
    }
}
