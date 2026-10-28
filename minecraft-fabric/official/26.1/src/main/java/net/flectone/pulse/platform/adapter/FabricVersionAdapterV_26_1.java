package net.flectone.pulse.platform.adapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_26_1 implements FabricVersionAdapter {

    private static final Permission TRUE_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.ALL);

    private MethodHandle jsonParseStringHandle;

    private CustomPacketPayload.Type<@NonNull ProxyPayload> payloadType;
    private StreamCodec<@NonNull FriendlyByteBuf, @NonNull ProxyPayload> streamCodec;

    @Inject
    public void resolveJsonParseString(ReflectionResolver reflectionResolver) {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        this.jsonParseStringHandle = jsonParserClass == null ? null : reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }

    @Override
    public Channel getChannel(ServerPlayer player) {
        return player.connection.connection.channel;
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.connection.latency();
    }

    @Override
    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    @Override
    public Component literal(String message) {
        return Component.literal(message);
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public Component fromJson(String json) {
        Object jsonElement = jsonParseStringHandle.invoke(json);
        DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

        return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
    }

    @Override
    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        return listener.authenticatedProfile;
    }

    @Override
    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.isAcceptingMessages();
    }

    @Override
    public UUID getId(GameProfile profile) {
        return profile.id();
    }

    @Override
    public String getName(GameProfile profile) {
        return profile.name();
    }

    @Override
    public PropertyMap getProperties(GameProfile profile) {
        return profile.properties();
    }

    @Override
    public String getValue(Property property) {
        return property.value();
    }

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.permissions().hasPermission(TRUE_PERMISSION);
    }

    @Override
    public @Nullable String getLanguage(ServerPlayer player) {
        return player.clientInformation().language();
    }

    @Override
    public ServerLevel getLevel(ServerPlayer player) {
        return player.level();
    }

    @Override
    public String getWorldName(ServerPlayer player) {
        return getLevel(player).dimension().identifier().getPath();
    }

    @Override
    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

    @Override
    public PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player) {
        return new PlatformPlayerAdapter.Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public boolean isSneaking(ServerPlayer player) {
        return player.isCrouching();
    }

    @Override
    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.getCustomName();
    }

    @Override
    public String getItemPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(byteBuf, minecraftServer.registryAccess()), itemStack);
    }

    @Override
    public @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getPlayer();
    }

    @Override
    public CommandSourceStack createCommandSourceStack(ServerPlayer player) {
        return player.createCommandSourceStack();
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendSystemMessage(component);
    }

    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> consumer.accept(dispatcher));
    }

    @Override
    public void registerProxyChannel(String channel, Consumer<byte[]> consumer) {
        payloadType = new CustomPacketPayload.Type<>(Identifier.tryParse(channel));

        if (streamCodec == null) {
            streamCodec = StreamCodec.of(
                    (buf, payload) -> buf.writeBytes(payload.data()),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(payloadType, data);
                    }
            );

            PayloadTypeRegistry.clientboundPlay().register(payloadType, streamCodec);
            PayloadTypeRegistry.serverboundPlay().register(payloadType, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, _) ->
                consumer.accept(payload.data())
        );
    }

    @Override
    public void unregisterProxyChannel() {
        ServerPlayNetworking.unregisterGlobalReceiver(payloadType.id());
        payloadType = null;
    }

    @Override
    public void sendProxyMessage(ServerPlayer player, byte[] data) {
        ServerPlayNetworking.send(player, new ProxyPayload(payloadType, data));
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

}
