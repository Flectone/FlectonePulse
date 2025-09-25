package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.geolocate.model.GeolocateMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
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
public class GeolocateModule extends AbstractModuleCommand<Localization.Command.Geolocate> {

    private final String apiUrl = "http://ip-api.com/line/<ip>?fields=17031449";

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public GeolocateModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_GEOLOCATE);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(manager -> manager
                .permission(permission().getName())
                .required(promptPlayer, commandParserProvider.playerParser(config().isSuggestOfflinePlayers()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);

        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::getNullPlayer)
                    .build()
            );

            return;
        }

        String ip = platformPlayerAdapter.isOnline(fTarget) ? platformPlayerAdapter.getIp(fTarget) : fTarget.getIp();

        List<String> response = ip == null ? List.of() : readResponse(Strings.CS.replace(apiUrl, "<ip>", ip));
        if (response.isEmpty() || response.get(0).equals("fail")) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::getNullOrError)
                    .build()
            );

            return;
        }

        sendMessage(GeolocateMetadata.<Localization.Command.Geolocate>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(geolocate -> StringUtils.replaceEach(geolocate.getFormat(),
                        new String[]{"<country>", "<region_name>", "<city>", "<timezone>", "<mobile>", "<proxy>", "<hosting>", "<query>"},
                        new String[]{response.get(1), response.get(2), response.get(3), response.get(4), response.get(5), response.get(6), response.get(7), response.get(8)}
                ))
                .response(response)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );

    }

    @Override
    public Command.Geolocate config() {
        return fileResolver.getCommand().getGeolocate();
    }

    @Override
    public Permission.Command.Geolocate permission() {
        return fileResolver.getPermission().getCommand().getGeolocate();
    }

    @Override
    public Localization.Command.Geolocate localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getGeolocate();
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
