package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_1_14 implements FabricVersionAdapter {

    private Field profileField;

    private ResourceLocation proxyChannel;

    @Override
    public Channel getChannel(ServerPlayer player) {
        return (Channel) PacketEvents.getAPI().getProtocolManager().getChannel(player.getUUID());
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.latency;
    }

    @Override
    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    @Override
    public Component literal(String message) {
        return new TextComponent(message);
    }

    @Override
    public Component fromJson(String json) {
        return Component.Serializer.fromJson(json);
    }

    @SneakyThrows
    @Override
    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        if (profileField == null) {
            profileField = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredFields())
                    .filter(field -> field.getType() == GameProfile.class)
                    .findFirst()
                    .orElseThrow();

            profileField.setAccessible(true);
        }

        GameProfile profile = (GameProfile) profileField.get(listener);
        if (profile == null || profile.getId() != null) return profile;

        return new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes(StandardCharsets.UTF_8)), profile.getName());
    }

    @Override
    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.connection.isConnected();
    }

    @Override
    public UUID getId(GameProfile profile) {
        return profile.getId();
    }

    @Override
    public String getName(GameProfile profile) {
        return profile.getName();
    }

    @Override
    public PropertyMap getProperties(GameProfile profile) {
        return profile.getProperties();
    }

    @Override
    public String getValue(Property property) {
        return property.getValue();
    }

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new GameProfile(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

    @Override
    public @Nullable String getLanguage(ServerPlayer player) {
        return null;
    }

    @Override
    public ServerLevel getLevel(ServerPlayer player) {
        return (ServerLevel) player.getCommandSenderWorld();
    }

    @Override
    public String getWorldName(ServerPlayer player) {
        return DimensionType.getName(player.dimension).getPath();
    }

    @Override
    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

    @Override
    public PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player) {
        BlockPos blockPos = new BlockPos(player.position());
        return new PlatformPlayerAdapter.Coordinates(blockPos.getX(), blockPos.getY(), blockPos.getZ(), player.yRot, player.xRot);
    }

    @Override
    public boolean isSneaking(ServerPlayer player) {
        return player.isSneaking();
    }

    @Override
    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = Registry.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(effect);
    }

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
    }

    @Override
    public String getItemPath(ItemStack itemStack) {
        return Registry.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        new FriendlyByteBuf(byteBuf).writeItem(itemStack);
    }

    @Override
    public @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getEntity() instanceof ServerPlayer player ? player : null;
    }

    @Override
    public CommandSourceStack createCommandSourceStack(ServerPlayer player) {
        return player.createCommandSourceStack();
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendMessage(component);
    }

    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> consumer.accept(dispatcher));
    }

    @Override
    public void registerProxyChannel(String channel, Consumer<byte[]> consumer) {
        proxyChannel = ResourceLocation.tryParse(channel);

        ServerPlayNetworking.registerGlobalReceiver(proxyChannel, (server, player, handler, buf, responseSender) ->
                consumer.accept(ByteBufUtil.getBytes(buf))
        );
    }

    @Override
    public void unregisterProxyChannel() {
        ServerPlayNetworking.unregisterGlobalReceiver(proxyChannel);
        proxyChannel = null;
    }

    @Override
    public void sendProxyMessage(ServerPlayer player, byte[] data) {
        ServerPlayNetworking.send(player, proxyChannel, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
    }

}
