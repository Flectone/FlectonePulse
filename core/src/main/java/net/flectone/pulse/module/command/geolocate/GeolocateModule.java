package net.flectone.pulse.module.command.geolocate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GeolocateModule extends AbstractModuleCommand<Localization.Command.Geolocate> {

    private final String HTTP_URL = "http://ip-api.com/line/<ip>?fields=17031449";

    private final Command.Geolocate command;
    private final Permission.Command.Geolocate permission;

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandRegistry commandRegistry;

    @Inject
    public GeolocateModule(FileManager fileManager,
                           FPlayerService fPlayerService,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getGeolocate(), null);

        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getGeolocate();
        permission = fileManager.getPermission().getCommand().getGeolocate();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String playerName = commandContext.get(promptPlayer);

        FPlayer fTarget = fPlayerService.getFPlayer(playerName);

        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Geolocate::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String ip = platformPlayerAdapter.isOnline(fTarget) ? fPlayerService.getIp(fTarget) : fTarget.getIp();

        List<String> request = ip == null ? List.of() : readResponse(HTTP_URL.replace("<ip>", ip));
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

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));

            String line;
            while((line = reader.readLine()) != null) {
                arrayList.add(line);
            }

            reader.close();
        } catch (IOException ignored) {}

        return arrayList;
    }
}
