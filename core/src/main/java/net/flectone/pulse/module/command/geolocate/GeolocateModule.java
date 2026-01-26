package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.geolocate.model.GeolocateMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeolocateModule extends AbstractModuleCommand<Localization.Command.Geolocate> {

    private static final String IP_API_URL = "http://ip-api.com/line/<ip>?fields=17031449";

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);

        if (fTarget.isUnknown()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Geolocate>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::nullPlayer)
                    .build()
            );

            return;
        }

        String ip = platformPlayerAdapter.isOnline(fTarget) ? platformPlayerAdapter.getIp(fTarget) : fTarget.getIp();

        List<String> response = ip == null ? List.of() : readResponse(Strings.CS.replace(IP_API_URL, "<ip>", ip));
        if (response.isEmpty() || response.get(0).equals("fail")) {
            sendErrorMessage(EventMetadata.<Localization.Command.Geolocate>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::nullOrError)
                    .build()
            );

            return;
        }

        sendMessage(GeolocateMetadata.<Localization.Command.Geolocate>builder()
                .base(EventMetadata.<Localization.Command.Geolocate>builder()
                        .sender(fPlayer)
                        .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fTarget)})
                        .format(geolocate -> StringUtils.replaceEach(geolocate.format(),
                                new String[]{"<country>", "<region_name>", "<city>", "<timezone>", "<mobile>", "<proxy>", "<hosting>", "<query>"},
                                new String[]{response.get(1), response.get(2), response.get(3), response.get(4), response.get(5), response.get(6), response.get(7), response.get(8)}
                        ))
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .response(response)
                .build()
        );

    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_GEOLOCATE;
    }

    @Override
    public Command.Geolocate config() {
        return fileFacade.command().geolocate();
    }

    @Override
    public Permission.Command.Geolocate permission() {
        return fileFacade.permission().command().geolocate();
    }

    @Override
    public Localization.Command.Geolocate localization(FEntity sender) {
        return fileFacade.localization(sender).command().geolocate();
    }

    private List<String> readResponse(String url) {
        List<String> arrayList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(url).toURL().openStream()))) {

            String line;
            while((line = reader.readLine()) != null) {
                arrayList.add(line);
            }

        } catch (IOException | URISyntaxException ignored) {
            // ignore, return empty list
        }

        return arrayList;
    }
}
