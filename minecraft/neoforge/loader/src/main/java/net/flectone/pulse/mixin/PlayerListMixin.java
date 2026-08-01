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

import net.flectone.pulse.LoaderBootstrap;
import net.flectone.pulse.NeoForgeFlectonePulseLoader;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@NullMarked
@Mixin(PlayerList.class)
public class PlayerListMixin {

    /**
     * @reason Associate connection instance with player instance
     */
    @Inject(
            method = "placeNewPlayer",
            at = @At("HEAD")
    )
    private void preNewPlayerPlace(
            Connection connection, ServerPlayer player,
            CommonListenerCookie cookie, CallbackInfo ci
    ) {
        LoaderBootstrap loaderBootstrap = NeoForgeFlectonePulseLoader.getPlugin();
        if (loaderBootstrap == null) return;

        loaderBootstrap.preNewPlayerPlace(connection, player);
    }

    /**
     * @reason Call login event and verify injection
     */
    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onPlayerLogin(
            Connection connection, ServerPlayer player,
            CommonListenerCookie cookie, CallbackInfo ci
    ) {
        LoaderBootstrap loaderBootstrap = NeoForgeFlectonePulseLoader.getPlugin();
        if (loaderBootstrap == null) return;

        loaderBootstrap.onPlayerLogin(connection, player);
    }

    /**
     * @reason Minecraft creates a new player instance on respawn
     */
    @Inject(
            method = "respawn",
            at = @At("RETURN")
    )
    private void postRespawn(CallbackInfoReturnable<ServerPlayer> cir) {
        LoaderBootstrap loaderBootstrap = NeoForgeFlectonePulseLoader.getPlugin();
        if (loaderBootstrap == null) return;

        loaderBootstrap.postRespawn(cir);
    }
}
