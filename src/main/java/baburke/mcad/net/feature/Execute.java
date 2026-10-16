package baburke.mcad.net.feature;

import baburke.mcad.net.network.UpdateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;

public class Execute {

    Minecraft client = Minecraft.getInstance();

    BlockPos pos = new BlockPos(10, -59, 0);
    //BlockState state = client.level.getBlockState(pos);
    BlockState state = Blocks.STONE.defaultBlockState();

    // Destroy (cut)
//                ClientPlayNetworking.send(
//                        new DestroyBlockPayload(pos)
//                );

    public void extrude() {

        // if (client.level.getBlockState(pos) == Blocks.STONE.defaultBlockState()) {return;}

        for (int i = 0; i < 10; i++) {
            pos = new BlockPos(pos.getX(), -59, i);
            ClientPlayNetworking.send(
                    new UpdateBlock(pos, state)
            );
        }

        // use a Map<BlockPos, BlockState> to build the structure, then convert it into a bounded List<BlockEntry> for each network batch.
        // This keeps storage convenient and network transfers manageable.


    }


    // To Send multiple blocks create payload
//                UpdateBlock payload = UpdateBlock.place(
//                        pos,
//                        Blocks.STONE.defaultBlockState()
//                );
//
//                ClientPlayNetworking.send(payload);



}
