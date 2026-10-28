package net.flectone.pulse.platform.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public interface FabricVersionAdapter {

    Channel getChannel(ServerPlayer player);

    int getPing(ServerPlayer player);

    void disconnect(ServerPlayer player, String message);

    Component literal(String message);

    Component fromJson(String json);

    @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener);

    boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener);

    UUID getId(GameProfile profile);

    String getName(GameProfile profile);

    PropertyMap getProperties(GameProfile profile);

    String getValue(Property property);

    boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name);

    boolean hasPermission(ServerPlayer player);

    @Nullable String getLanguage(ServerPlayer player);

    ServerLevel getLevel(ServerPlayer player);

    String getWorldName(ServerPlayer player);

    @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid);

    PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player);

    boolean isSneaking(ServerPlayer player);

    @Nullable MobEffectInstance getEffect(ServerPlayer player, int id);

    @Nullable Component getCustomName(ItemStack itemStack);

    String getItemPath(ItemStack itemStack);

    void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer);

    @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack);

    CommandSourceStack createCommandSourceStack(ServerPlayer player);

    void sendSystemMessage(ServerPlayer player, Component component);

    void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer);

    void registerProxyChannel(String channel, Consumer<byte[]> consumer);

    void unregisterProxyChannel();

    void sendProxyMessage(ServerPlayer player, byte[] data);

}
