package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GeolocateModule extends AbstractModuleCommand<Localization.Command.Geolocate> {

    private final String apiUrl = "http://ip-api.com/line/<ip>?fields=17031449";

    private final Command.Geolocate command;
    private final Permission.Command.Geolocate permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public GeolocateModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getGeolocate(), Command::getGeolocate);

        this.command = fileResolver.getCommand().getGeolocate();
        this.permission = fileResolver.getPermission().getCommand().getGeolocate();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser(command.isSuggestOfflinePlayers()))
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);

        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Geolocate::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String ip = platformPlayerAdapter.isOnline(fTarget) ? platformPlayerAdapter.getIp(fTarget) : fTarget.getIp();

        List<String> request = ip == null ? List.of() : readResponse(apiUrl.replace("<ip>", ip));
        if (request.isEmpty() || request.get(0).equals("fail")) {
            builder(fPlayer)
                    .format(Localization.Command.Geolocate::getNullOrError)
                    .sendBuilt();
            return;
        }

        builder(fTarget)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat()
                        .replace("<country>", request.get(1))
                        .replace("<region_name>", request.get(2))
                        .replace("<city>", request.get(3))
                        .replace("<timezone>", request.get(4))
                        .replace("<mobile>", request.get(5))
                        .replace("<proxy>", request.get(6))
                        .replace("<hosting>", request.get(7))
                        .replace("<query>", request.get(8))
                )
                .sound(getSound())
                .sendBuilt();
    }

    private List<String> readResponse(String url) {
        List<String> arrayList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader((new URL(url)).openStream()))) {

            String line;
            while((line = reader.readLine()) != null) {
                arrayList.add(line);
            }

        } catch (IOException ignored) {
            // ignore, return empty list
        }

        return arrayList;
    }
}
