package net.flectone.pulse.module.command.chatcolor;

import java.util.LinkedHashSet;
import java.util.Collections;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.chatcolor.listener.ChatcolorProxyMessageListener;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatcolorModuleImpl implements ChatcolorModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final ColorConverter colorConverter;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;
    private final ProxyRegistry proxyRegistry;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptColor = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::color);
        String promptPlayer = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, commandBuilder -> {
            commandBuilder = commandBuilder
                    .permission(permission().name())
                    .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion());

            for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
                commandBuilder = commandBuilder.optional(promptColor + " " + (i + 1), commandParserProvider.colorParser());
            }

            return commandBuilder.optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission().other()));
        });

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(ChatcolorProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(ChatcolorModule.super.permissions());
        permissions.add(permission().other());
        permissions.addAll(permission().colors().values());
        return Collections.unmodifiableSet(permissions);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, _) -> Arrays.stream(FColor.Type.values())
                .filter(type -> permissionChecker.check(context.sender(), permission().colors().get(type.name())))
                .map(setting -> Suggestion.suggestion(setting.name().toLowerCase()))
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String type = commandModuleController.getArgument(this, commandContext, 0);
        Optional<FColor.Type> fColorType = switch (type.toLowerCase()) {
            case "out" -> Optional.of(FColor.Type.OUT);
            case "see" -> Optional.of(FColor.Type.SEE);
            default -> Optional.empty();
        };

        if (fColorType.isEmpty() || !permissionChecker.check(fPlayer, permission().colors().get(fColorType.get().name()))) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullType())
                            .build()
                    )
                    .build()
            );

            return;
        }

        boolean hasOtherPermission = permissionChecker.check(fPlayer, permission().other());

        FPlayer fTarget = fPlayer;

        String promptPlayer = commandModuleController.getPrompt(this, 2);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);
        if (optionalTarget.isPresent() && hasOtherPermission) {
            fTarget = fPlayerService.getFPlayer(optionalTarget.get());
            if (fTarget.isUnknown()) {
                fTarget = fPlayer;
            }
        }

        String promptColor = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalClear = commandContext.optional(promptColor + " 1");
        if (optionalClear.isPresent() && optionalClear.get().equalsIgnoreCase("clear")) {
            setColors(fTarget, fColorType.get(), Set.of());
            return;
        }

        LinkedHashMap<Integer, FColor> newFColors = new LinkedHashMap<>();
        socialService.loadColors(fTarget).getOrDefault(fColorType.get(), Set.of())
                .forEach(fColor -> newFColors.put(fColor.number(), fColor));

        for (int i = 0; i < fColorConfig().defaultColors().size(); i++) {
            Optional<String> optionalColor = commandContext.optional(promptColor + " " + (i + 1));
            if (optionalColor.isEmpty()) continue;

            String name = hasOtherPermission
                    ? optionalColor.get() // allow any input
                    : colorConverter.isCorrect(optionalColor.get().toLowerCase());
            if (name == null || name.equals("null")) continue;

            int number = i + 1;
            FColor fColor = new FColor(number, StringUtils.left(name, 255));

            newFColors.put(number, fColor);
        }

        if (newFColors.isEmpty()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullColor())
                            .build()
                    )
                    .build()
            );

            return;
        }

        setColors(fTarget, fColorType.get(), new HashSet<>(newFColors.values()));
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_CHATCOLOR;
    }

    @Override
    public Command.Chatcolor config() {
        return fileFacade.command().chatcolor();
    }

    @Override
    public Permission.Command.Chatcolor permission() {
        return fileFacade.permission().command().chatcolor();
    }

    @Override
    public Localization.Command.Chatcolor localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().chatcolor();
    }

    @Override
    public Message.Format.FColor fColorConfig() {
        return fileFacade.message().format().fcolor();
    }

    private void setColors(FPlayer fPlayer, FColor.Type type, Set<FColor> newFColors) {
        Map<FColor.Type, Set<FColor>> fColors = socialService.loadColors(fPlayer);
        Set<FColor> oldFColors = fColors.getOrDefault(type, Set.of());

        UUID metadataUUID = UUID.randomUUID();

        if (!oldFColors.equals(newFColors)) {
            socialService.saveColors(fPlayer, type, newFColors);
        }

        if (!fPlayer.isOnline()) return;
        if (proxySender.send(fPlayer, ModuleName.COMMAND_CHATCOLOR, _ -> {}, metadataUUID)) return;

        sendMessageWithUpdatedColors(fPlayer, metadataUUID);
    }

    @Override
    public void sendMessageWithUpdatedColors(FPlayer fPlayer, UUID metadataUUID) {
        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .uuid(metadataUUID)
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(localization(fResolver).format())
                        .build()
                )
                .build()
        );
    }
}
