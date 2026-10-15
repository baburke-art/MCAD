package baburke.mcad.net;

import baburke.mcad.net.block.ModBlocks;
import baburke.mcad.net.feature.DestroyBlockPayload;
import baburke.mcad.net.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MCAD implements ModInitializer {
	public static final String MOD_ID = "mcad";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//LOGGER.info("Hello Fabric world!");

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		PayloadTypeRegistry.serverboundPlay().register(
				DestroyBlockPayload.TYPE,
				DestroyBlockPayload.CODEC
		);

		ServerPlayNetworking.registerGlobalReceiver(
				DestroyBlockPayload.TYPE,
				(payload, context) -> {
					var player = context.player();
					var level = player.level();
					var pos = payload.pos();

					// Basic validation
					if (player.blockPosition().distSqr(pos) > 36) {
						return;
					}

					if (!level.isLoaded(pos)) {
						return;
					}

					if (level.getBlockState(pos).isAir()) {
						return;
					}

					level.destroyBlock(pos, true, player);
				}
		);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("mcad", path);
	}
}