package baburke.mcad.net.feature;

import baburke.mcad.net.network.UpdateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Execute {

    // Plane plane = new Plane();

    // if (!plane == null)

    BlockPos pos = new BlockPos(10, -60, 0);
    //BlockState state = client.level.getBlockState(pos);
    BlockState state = Blocks.STONE.defaultBlockState();

    // This is only client side
    //client.level.destroyBlock(pos, true, client.player);

    // Destroy (cut)
//                ClientPlayNetworking.send(
//                        new DestroyBlockPayload(pos)
//                );

    public void extrude() {
        for (int i = 0; i < 10; i++) {
            pos = new BlockPos(pos.getX(), i, pos.getZ());
            ClientPlayNetworking.send(
                    new UpdateBlock(pos, Blocks.STONE.defaultBlockState())
            );
        }

    }


    // To Send multiple blocks create payload
//                UpdateBlock payload = UpdateBlock.place(
//                        pos,
//                        Blocks.STONE.defaultBlockState()
//                );
//
//                ClientPlayNetworking.send(payload);



}
