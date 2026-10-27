package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.flectone.pulse.platform.proxy.FabricProxyChannel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapter {

    public Channel getChannel(ServerPlayer player) {
        return player.connection.connection.channel;
    }

    public int getPing(ServerPlayer player) {
        return player.connection.latency();
    }

    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    public Component literal(String message) {
        return Component.literal(message);
    }

    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        return listener.authenticatedProfile;
    }

    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.isAcceptingMessages();
    }

    public UUID getId(GameProfile profile) {
        return profile.id();
    }

    public String getName(GameProfile profile) {
        return profile.name();
    }

    public PropertyMap getProperties(GameProfile profile) {
        return profile.properties();
    }

    public String getValue(Property property) {
        return property.value();
    }

    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, name));
    }

    public boolean hasPermission(ServerPlayer player) {
        return player.permissions().hasPermission(TruePermission.INSTANCE);
    }

    public @Nullable String getLanguage(ServerPlayer player) {
        return player.clientInformation().language();
    }

    public ServerLevel getLevel(ServerPlayer player) {
        return player.level();
    }

    public String getWorldName(ServerPlayer player) {
        return getLevel(player).dimension().identifier().getPath();
    }

    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

    public PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player) {
        return new PlatformPlayerAdapter.Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
    }

    public boolean isSneaking(ServerPlayer player) {
        return player.isCrouching();
    }

    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }

    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.getCustomName();
    }

    public String getItemPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(byteBuf, minecraftServer.overworld().registryAccess()), itemStack);
    }

    public @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getPlayer();
    }

    public CommandSourceStack createCommandSourceStack(ServerPlayer player) {
        return player.createCommandSourceStack();
    }

    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendSystemMessage(component);
    }

    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register(new CommandRegistrationCallback() {
            @Override
            public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
                consumer.accept(dispatcher);
            }
        });
    }

    public void registerProxyChannel(String channel, Consumer<byte[]> consumer) {
        FabricProxyChannel.register(channel, consumer);
    }

    public void unregisterProxyChannel() {
        FabricProxyChannel.unregister();
    }

    public void sendProxyMessage(ServerPlayer player, byte[] data) {
        FabricProxyChannel.send(player, data);
    }

    private static final class TruePermission {

        private static final Permission INSTANCE = new Permission.HasCommandLevel(PermissionLevel.ALL);

    }

}
