/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2024 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.flectone.pulse.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.flectone.pulse.FabricFlectonePulseLoader;
import net.flectone.pulse.constant.HookType;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NullMarked
@Mixin(value = Connection.class, priority = 1500)
public class ConnectionMixin {

    @Shadow
    private Channel channel;

    @Inject(
            method = "configureSerialization*",
            at = @At("TAIL"),
            require = 0
    )
    private static void configureSerialization(
            CallbackInfo ci,
            @Local(argsOnly = true) ChannelPipeline pipeline,
            @Local(argsOnly = true) PacketFlow flow
    ) {
        FabricFlectonePulseLoader.getLoaderBootstrap().hook(HookType.CONFIGURE_SERIALIZATION, pipeline, flow);
    }

    @Inject(
            method = "setupCompression*",
            at = @At("TAIL")
    )
    private void setupCompression(CallbackInfo ci) {
        if (channel == null || !channel.isActive()) return;

        FabricFlectonePulseLoader.getLoaderBootstrap().hook(HookType.SETUP_COMPRESSION, channel.pipeline());
    }

}
