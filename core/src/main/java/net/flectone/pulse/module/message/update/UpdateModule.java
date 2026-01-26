package net.flectone.pulse.module.message.update;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.update.listener.UpdatePulseListener;
import net.flectone.pulse.module.message.update.model.UpdateMessageMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.comparator.VersionComparator;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UpdateModule extends AbstractModuleLocalization<Localization.Message.Update> {

    private final FileFacade fileFacade;
    private final VersionComparator versionComparator;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final Gson gson;

    private String latestVersion;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(UpdatePulseListener.class);

        checkAndUpdateLatestVersion();
    }

    @Override
    public MessageType messageType() {
        return MessageType.UPDATE;
    }

    @Override
    public Message.Update config() {
        return fileFacade.message().update();
    }

    @Override
    public Permission.Message.Update permission() {
        return fileFacade.permission().message().update();
    }

    @Override
    public Localization.Message.Update localization(FEntity sender) {
        return fileFacade.localization(sender).message().update();
    }
    
    public void send(FPlayer fPlayer) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (isModuleDisabledFor(fPlayer)) return;
            if (latestVersion == null) return;

            String currentVersion = fileFacade.config().version();
            if (!versionComparator.isOlderThan(currentVersion, latestVersion)) return;

            sendMessage(UpdateMessageMetadata.<Localization.Message.Update>builder()
                    .base(EventMetadata.<Localization.Message.Update>builder()
                            .sender(fPlayer)
                            .format((fResolver, s) -> StringUtils.replaceEach(
                                    fResolver.isUnknown() ? s.formatConsole() : s.formatPlayer(),
                                    new String[]{"<current_version>", "<latest_version>"},
                                    new String[]{String.valueOf(currentVersion), String.valueOf(latestVersion)}
                            ))
                            .destination(config().destination())
                            .sound(soundOrThrow())
                            .build()
                    )
                    .currentVersion(currentVersion)
                    .latestVersion(latestVersion)
                    .build()
            );
        });
    }

    private static class LatestRelease {
        @SerializedName("tag_name")
        public String tagName;
    }

    private void checkAndUpdateLatestVersion() {
        taskScheduler.runAsync(() -> {
            HttpClient client = HttpClient.newHttpClient();
            try {
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
                // ignore exception
            }
        });
    }
}
