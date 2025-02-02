package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiReloadManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;

/**
 * This entire mixin assumes that no one will modify how recipes and tags are synced.
 * In vanilla, first connect gets them in one order, and then reloads send them reversed.
 * This waits for both, then reloads.
 * If only one comes, no reload will occur, which would be weird behavior.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Unique
	private int infoMask = 0;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void init(CallbackInfo info) {
		EmiReloadManager.clear();
	}

	@Inject(at = @At("RETURN"), method = "onSynchronizeRecipes")
	private void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet, CallbackInfo info) {
		infoMask |= 1;
		if (infoMask == 3) {
			infoMask = 0;
			EmiReloadManager.reload();
		}
	}

	@Inject(at = @At("RETURN"), method = "onSynchronizeTags")
	private void onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo info) {
		infoMask |= 2;
		if (infoMask == 3) {
			infoMask = 0;
			EmiReloadManager.reload();
		}
	}
}
