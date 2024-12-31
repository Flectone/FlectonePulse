package net.flectone.pulse.mixin;

import net.flectone.pulse.scheduler.BlfSchedulerTicker;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class ExampleMixin {

	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadWorld()V
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		BlfSchedulerTicker.tick();
	}


}