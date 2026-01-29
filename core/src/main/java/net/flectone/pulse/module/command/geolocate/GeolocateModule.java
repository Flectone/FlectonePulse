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
import net.flectone.pulse.module.command.geolocate.model.IpResponse;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeolocateModule extends AbstractModuleCommand<Localization.Command.Geolocate> {

    private static final String IP_API_URL = "http://ip-api.com/json/<ip>?fields=17031449";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;

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
        if (StringUtils.isEmpty(ip)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Geolocate>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::nullOrError)
                    .build()
            );
            return;
        }

        IpResponse response = getGeolocation(ip);
        if (response == null || !response.isSuccess()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Geolocate>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Geolocate::nullOrError)
                    .build()
            );
            return;
        }

        String userCurrentTime = getUserCurrentTime(response);

        sendMessage(GeolocateMetadata.<Localization.Command.Geolocate>builder()
                .base(EventMetadata.<Localization.Command.Geolocate>builder()
                        .sender(fPlayer)
                        .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fTarget)})
                        .format(geolocate -> StringUtils.replaceEach(geolocate.format(),
                                new String[]{"<country>", "<region_name>", "<city>", "<timezone>", "<mobile>", "<proxy>", "<hosting>", "<query>", "<current_time>"},
                                new String[]{response.country(), response.region(), response.city(), response.timezone() , String.valueOf(response.mobile()), String.valueOf(response.proxy()), String.valueOf(response.hosting()), response.query(), userCurrentTime}
                        ))
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .response(response)
                .build()
        );
    }

    private IpResponse getGeolocation(String ip) {
        try {
            String url = IP_API_URL.replace("<ip>", ip);
            String json = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8)
                    .useDelimiter("\\A")
                    .next();

            return objectMapper.readValue(json, IpResponse.class);
        } catch (IOException e) {
            return null;
        }
    }

    private String getUserCurrentTime(IpResponse response) {
        try {
            ZonedDateTime userTime;

            if (response.timezone() != null && !response.timezone().isEmpty()) {
                userTime = ZonedDateTime.now(ZoneId.of(response.timezone()));
            } else if (response.offset() != null) {
                ZoneOffset offset = ZoneOffset.ofTotalSeconds(response.offset());
                userTime = Instant.now().atOffset(offset).toZonedDateTime();
            } else {
                userTime = ZonedDateTime.now(ZoneOffset.UTC);
            }

            return timeFormatter.formatDate(userTime.toInstant().toEpochMilli());

        } catch (Exception e) {
            return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        }
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

}