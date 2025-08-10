package net.flectone.pulse.module.message.advancement;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.advancement.listener.AdvancementPulseListener;
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;
import net.flectone.pulse.module.message.advancement.model.CommandAdvancement;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class AdvancementModule extends AbstractModuleLocalization<Localization.Message.Advancement> {

    private final Message.Advancement message;
    private final Permission.Message.Advancement permission;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final Gson gson;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AdvancementModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             IntegrationModule integrationModule,
                             MessagePipeline messagePipeline,
                             Gson gson,
                             ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getAdvancement());

        this.message = fileResolver.getMessage().getAdvancement();
        this.permission = fileResolver.getPermission().getMessage().getAdvancement();
        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
        this.gson = gson;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(AdvancementPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fReceiver, ChatAdvancement chatAdvancement) {
        FPlayer fTarget = fPlayerService.getFPlayer(chatAdvancement.owner());
        if (fTarget.isUnknown()) return;

        if (isModuleDisabledFor(fTarget)) return;
        if (!fTarget.equals(fReceiver)) return;

        builder(fTarget)
                .range(message.getRange())
                .destination(message.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.ADVANCEMENT))
                .filter(fPlayer -> integrationModule.isVanishedVisible(fTarget, fPlayer))
                .tag(MessageType.ADVANCEMENT)
                .format(s -> convert(s, chatAdvancement))
                .tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fResolver, chatAdvancement)})
                .proxy(output -> output.writeUTF(gson.toJson(chatAdvancement)))
                .integration()
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(boolean revoke, FPlayer fPlayer, CommandAdvancement commandAdvancement) {
        if (commandAdvancement.isIncorrect()) return;
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(commandAdvancement.owner());
        if (fTarget.isUnknown()) return;

        Builder builder = builder(fTarget)
                .receiver(fPlayer)
                .format(s -> {
                    Localization.Message.Advancement.Command subcommand = revoke ? s.getRevoke() : s.getGrant();

                    return switch (commandAdvancement.relation()) {
                        case MANY_TO_ONE -> Strings.CS.replace(
                                subcommand.getManyToOne(),
                                "<number>", String.valueOf(commandAdvancement.content())
                        );
                        case ONE_TO_ONE_TEXT -> Strings.CS.replace(
                                subcommand.getOneToOne(),
                                "<advancement>", String.valueOf(commandAdvancement.content())
                        );
                        case ONE_TO_ONE_ADVANCEMENT -> subcommand.getOneToOne();
                    };
                })
                .sound(getSound());

        if (commandAdvancement.relation() == Relation.ONE_TO_ONE_ADVANCEMENT && commandAdvancement.chatAdvancement() != null) {
            builder.tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fPlayer, commandAdvancement.chatAdvancement())});
        }

        builder.sendBuilt();
    }

    public String convert(Localization.Message.Advancement message, ChatAdvancement chatAdvancement) {
        String string = switch (chatAdvancement.type()) {
            case CHAT_TYPE_ACHIEVEMENT_TAKEN -> message.getTaken().getFormat();
            case CHAT_TYPE_ADVANCEMENT_GOAL -> message.getGoal().getFormat();
            case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> message.getChallenge().getFormat();
            default -> message.getTask().getFormat();
        };

        return StringUtils.replaceEach(
                string,
                new String[]{"<title>", "<description>"},
                new String[]{String.valueOf(chatAdvancement.title()), String.valueOf(chatAdvancement.description())}
        );
    }

    public TagResolver advancementTag(FEntity sender, FPlayer receiver, @NotNull ChatAdvancement chatAdvancement) {
        String tag = "advancement";
        if (!isEnable()) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Localization.Message.Advancement localization = resolveLocalization(receiver);

            String title = switch (chatAdvancement.type()) {
                case CHAT_TYPE_ACHIEVEMENT_TAKEN -> localization.getTaken().getTag();
                case CHAT_TYPE_ADVANCEMENT_GOAL -> localization.getGoal().getTag();
                case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> localization.getChallenge().getTag();
                default -> localization.getTask().getTag();
            };

            title = StringUtils.replaceEach(
                    title,
                    new String[]{"<title>", "<description>"},
                    new String[]{String.valueOf(chatAdvancement.title()), String.valueOf(chatAdvancement.description())}
            );

            Component component = messagePipeline.builder(sender, receiver, title).build();
            return Tag.inserting(component);
        });
    }

    public enum Relation {
        MANY_TO_ONE,
        ONE_TO_ONE_ADVANCEMENT,
        ONE_TO_ONE_TEXT
    }
}
