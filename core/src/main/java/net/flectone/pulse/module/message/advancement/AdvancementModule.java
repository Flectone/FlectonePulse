package net.flectone.pulse.module.message.advancement;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.advancement.listener.AdvancementPacketListener;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Singleton
public class AdvancementModule extends AbstractModuleMessage<Localization.Message.Advancement> {

    @Getter private final Message.Advancement message;
    private final Permission.Message.Advancement permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;
    private final ComponentUtil componentUtil;
    private final Gson gson;

    @Inject
    public AdvancementModule(FileManager fileManager,
                             FPlayerManager fPlayerManager,
                             ListenerRegistry listenerRegistry,
                             IntegrationModule integrationModule,
                             ComponentUtil componentUtil,
                             Gson gson) {
        super(localization -> localization.getMessage().getAdvancement());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;
        this.componentUtil = componentUtil;
        this.gson = gson;

        message = fileManager.getMessage().getAdvancement();
        permission = fileManager.getPermission().getMessage().getAdvancement();

        addPredicate(integrationModule::isVanished);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(AdvancementPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID uuid, String target, Advancement advancement) {
        FPlayer fTarget = fPlayerManager.getOnline(target);
        if (fTarget.isUnknown()) return;
        if (checkModulePredicates(fTarget)) return;

        FPlayer fReceiver = fPlayerManager.get(uuid);
        if (!fTarget.equals(fReceiver)) return;

        builder(fTarget)
                .range(message.getRange())
                .destination(message.getDestination())
                .tag(MessageTag.ADVANCEMENT)
                .format(s -> convert(s, advancement))
                .tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fResolver, advancement)})
                .proxy(output -> output.writeUTF(gson.toJson(advancement)))
                .integration()
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(Relation relation,
                     boolean revoke,
                     UUID uuid,
                     String target,
                     @Nullable Advancement advancement,
                     @Nullable String content) {
        if (advancement == null && content == null) return;

        FPlayer fPlayer = fPlayerManager.get(uuid);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerManager.getOnline(target);
        if (fTarget.isUnknown()) return;

        Builder builder = builder(fTarget)
                .receiver(fPlayer)
                .format(s -> {
                    Localization.Message.Advancement.Command subcommand = revoke ? s.getRevoke() : s.getGrant();

                    return switch (relation) {
                        case MANY_TO_ONE -> subcommand.getManyToOne().replace("<number>", content);
                        case ONE_TO_ONE_TEXT -> subcommand.getOneToOne().replace("<advancement>", content);
                        case ONE_TO_ONE_ADVANCEMENT -> subcommand.getOneToOne();
                    };
                })
                .sound(getSound());

        if (relation == Relation.ONE_TO_ONE_ADVANCEMENT && advancement != null) {
            builder.tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fResolver, advancement)});
        }

        builder.sendBuilt();
    }

    public String convert(Localization.Message.Advancement message, Advancement advancement) {
        String string = switch (advancement.type()) {
            case TASK -> message.getTask().getFormat();
            case GOAL -> message.getGoal().getFormat();
            case CHALLENGE -> message.getChallenge().getFormat();
        };

        return string
                .replace("<title>", advancement.title())
                .replace("<description>", advancement.description());
    }

    public TagResolver advancementTag(FEntity sender, FPlayer receiver, @NotNull Advancement advancement) {
        return TagResolver.resolver("advancement", (argumentQueue, context) -> {
            if (!isEnable()) return Tag.selfClosingInserting(Component.empty());

            Localization.Message.Advancement localization = resolveLocalization(receiver);

            String title = switch (advancement.type()) {
                case TASK -> localization.getTask().getTag();
                case GOAL -> localization.getGoal().getTag();
                case CHALLENGE -> localization.getChallenge().getTag();
            };

            Component component = componentUtil.builder(sender, receiver, title
                            .replace("<title>", advancement.title())
                            .replace("<description>", advancement.description())
                    )
                    .build();

            return Tag.inserting(component);
        });
    }

    public enum Relation {
        MANY_TO_ONE,
        ONE_TO_ONE_ADVANCEMENT,
        ONE_TO_ONE_TEXT
    }
}
