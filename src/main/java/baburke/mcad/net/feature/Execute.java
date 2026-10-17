package baburke.mcad.net.feature;

import baburke.mcad.net.network.UpdateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class Execute {

    //BlockPos pos = new BlockPos(10, -59, 0);
    //BlockState state = client.level.getBlockState(pos);
    //BlockState state = Blocks.STONE.defaultBlockState();

    // Destroy (cut)
//                ClientPlayNetworking.send(
//                        new DestroyBlockPayload(pos)
//                );

    public void extrude(Minecraft client, Map<BlockPos, BlockState> blocks, int length) {

        // For every block in map
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            BlockPos nextPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ()+1);

//            ClientPlayNetworking.send(
//                    new UpdateBlock(nextPos, state)
//            );

//            for (int i = 1; i < length; i++) {
//                // How to account for direction changes? Orientation
//                int posZ = pos.getZ()+i;
//
//                BlockPos nextPos = new BlockPos(pos.getX(), pos.getY(), posZ);
//
//                ClientPlayNetworking.send(
//                        new UpdateBlock(nextPos, state)
//                );
//            }

            //System.out.println(pos.getX() + "," + pos.getY() + "," + pos.getZ() + " : " + state);

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
