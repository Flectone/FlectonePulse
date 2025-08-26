package net.flectone.pulse.module.message.update;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.update.model.UpdateMessageMetadata;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.update.listener.UpdatePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
public class UpdateModule extends AbstractModuleLocalization<Localization.Message.Update> {

    private final Message.Update message;
    private final Permission.Message.Update permission;
    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final Gson gson;

    private String latestVersion;

    @Inject
    public UpdateModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry,
                        Gson gson) {
        super(localization -> localization.getMessage().getUpdate(), MessageType.UPDATE);

        this.message = fileResolver.getMessage().getUpdate();
        this.permission = fileResolver.getPermission().getMessage().getUpdate();
        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
        this.gson = gson;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(UpdatePulseListener.class);

        checkAndUpdateLatestVersion();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (latestVersion == null) return;

        String currentVersion = fileResolver.getConfig().getVersion();
        if (!fileResolver.isVersionOlderThan(currentVersion, latestVersion)) return;

        sendMessage(UpdateMessageMetadata.<Localization.Message.Update>builder()
                .sender(fPlayer)
                .format((fResolver, s) -> StringUtils.replaceEach(
                        fResolver.isUnknown() ? s.getFormatConsole() : s.getFormatPlayer(),
                        new String[]{"<current_version>", "<latest_version>"},
                        new String[]{String.valueOf(currentVersion), String.valueOf(latestVersion)}
                ))
                .currentVersion(currentVersion)
                .latestVersion(latestVersion)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

    private static class LatestRelease {
        @SerializedName("tag_name")
        public String tagName;
    }

    @Async
    public void checkAndUpdateLatestVersion() {
        try (HttpClient client = HttpClient.newHttpClient()){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/Flectone/FlectonePulse/releases/latest"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;

            LatestRelease latestRelease = gson.fromJson(response.body(), LatestRelease.class);
            latestVersion = Strings.CS.replace(latestRelease.tagName, "v", "");

            // send to console
            send(FPlayer.UNKNOWN);
        } catch (IOException | InterruptedException ignored) {
        }
    }
}
