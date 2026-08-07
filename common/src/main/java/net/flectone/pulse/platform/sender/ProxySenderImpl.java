package net.flectone.pulse.platform.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.exception.ProxyMessageCreateException;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxySenderImpl implements ProxySender {

    private final ProxyRegistry proxyRegistry;
    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final PlatformServerAdapter platformServerAdapter;
    private final Gson gson;
    private final FLogger fLogger;

    public static void send(byte @NonNull [] data, @NonNull Consumer<byte[]> dataConsumer, @NonNull Consumer<UUID> backendJoinConfirm) {
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data))) {

            String tag = dataInputStream.readUTF();
            if (!tag.startsWith("FlectonePulse")) return;

            ModuleName proxyMessageType = ModuleName.fromProxyString(tag);
            if (proxyMessageType == null) return;
            if (proxyMessageType == ModuleName.PLAYER_CONNECTED) {
                UUID playerUUID = UUID.fromString(dataInputStream.readUTF());
                backendJoinConfirm.accept(playerUUID);
                return;
            }

            dataConsumer.accept(data);
        } catch (IOException e) {
            throw new ProxyMessageCreateException(e);
        }
    }

    public static void send(@NonNull ModuleName tag, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer, @NonNull Consumer<byte[]> dataConsumer) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());

            outputConsumer.accept(output);

            dataConsumer.accept(byteStream.toByteArray());
        } catch (IOException e) {
            throw new ProxyMessageCreateException(e);
        }
    }

    @Override
    public boolean send(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata, @NonNull MessageContext messageContext) {
        ProxyDataConsumer<DataOutputStream> proxyConsumer = eventMetadata.proxy();
        if (proxyConsumer == null) return false;

        Range range = eventMetadata.range();
        if (!range.is(Range.Type.PROXY)) return false;

        return send(messageContext.sender(), moduleName, proxyConsumer, messageContext.uuid());
    }

    @Override
    public boolean send(@NonNull FEntity sender, @NonNull ModuleName tag) {
        return send(sender, tag, _ -> {}, UUID.randomUUID());
    }

    @Override
    public boolean send(@NonNull FEntity sender, @NonNull ModuleName tag, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer) {
        return send(sender, tag, outputConsumer, UUID.randomUUID());
    }

    @Override
    public boolean send(@NonNull FEntity sender,
                        @NonNull ModuleName tag,
                        @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer,
                        @NonNull UUID metadataUUID) {
        if (!proxyRegistry.hasEnabledProxy()) return false;
        if (FlectonePulseAPI.isDisabling()) return false;

        if (sender instanceof FPlayer fPlayer) {
            List<String> constant = fileFacade.localization().message().format().names().constant();
            if (!constant.isEmpty()) {
                sender = fPlayer.withConstants(constant.stream()
                        .map(string -> messagePipeline.build(MessageContext.builder()
                                .sender(fPlayer)
                                .message(string)
                                .build()
                        ))
                        .toList()
                );
            }
        }

        byte[] message;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());
            output.writeUTF(metadataUUID.toString());

            output.writeUTF(fileFacade.config().server());

            // this parameter is always different from config value, because it is taken relative to worlds.
            // this is to prevent user from creating two identical server
            output.writeUTF(platformServerAdapter.getServerUUID());

            output.writeUTF(gson.toJson(fileFacade.config().proxy().clusters()));
            output.writeUTF(gson.toJson(sender));
            outputConsumer.accept(output);

            message = byteStream.toByteArray();
        } catch (IOException e) {
            fLogger.warning(e);
            return false;
        }

        boolean sent = false;
        for (Proxy proxy : proxyRegistry.getProxies()) {
            if (proxy.sendMessage(sender, tag, message)) {
                sent = true;
            }
        }

        return sent;
    }

}
