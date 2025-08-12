package net.flectone.pulse.module.message.format.replacement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ReplacementPulseListener implements PulseListener {

    private final Permission.Message.Format.Replacement permission;
    private final ReplacementModule replacementModule;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public ReplacementPulseListener(FileResolver fileResolver,
                                    ReplacementModule replacementModule,
                                    MessagePipeline messagePipeline,
                                    PermissionChecker permissionChecker) {
        this.permission = fileResolver.getPermission().getMessage().getFormat().getReplacement();
        this.replacementModule = replacementModule;
        this.messagePipeline = messagePipeline;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.REPLACEMENT)) return;

        FEntity sender = messageContext.getSender();
        if (replacementModule.isModuleDisabledFor(sender)) return;

        String processedMessage = replacementModule.processMessage(messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FPlayer receiver = messageContext.getReceiver();
        boolean isTranslateItem = messageContext.isFlag(MessageFlag.TRANSLATE_ITEM);

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.REPLACEMENT, (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            String name = argument.value();
            if (!permissionChecker.check(sender, permission.getValues().get(name))) return Tag.selfClosingInserting(Component.empty());

            String replacement = replacementModule.resolveLocalization(receiver).getValues().get(name);
            if (replacement == null) return Tag.selfClosingInserting(Component.empty());


            List<String> values = new ArrayList<>();
            while (argumentQueue.hasNext()) {
                Tag.Argument groupArg = argumentQueue.pop();
                values.add(StringEscapeUtils.unescapeJava(groupArg.value()));
            }

            return switch (name) {
                case "ping" -> replacementModule.pingTag(sender, receiver);
                case "tps" -> replacementModule.tpsTag(sender, receiver);
                case "online" -> replacementModule.onlineTag(sender, receiver);
                case "coords" -> replacementModule.coordsTag(sender, receiver);
                case "stats" -> replacementModule.statsTag(sender, receiver);
                case "skin" -> replacementModule.skinTag(sender, receiver);
                case "item" -> replacementModule.itemTag(sender, receiver, isTranslateItem);
                case "url" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield replacementModule.urlTag(sender, receiver, values.get(1));
                }
                case "image" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield replacementModule.imageTag(sender, receiver, values.get(1));
                }
                case "spoiler" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield replacementModule.spoilerTag(sender, receiver, values.get(1), messageContext.getFlags());
                }
                default -> {
                    String[] searchList = new String[values.size()];
                    String[] replacementList = new String[values.size()];

                    for (int i = 0; i < values.size(); i++) {
                        searchList[i] = "<message_" + i + ">";
                        replacementList[i] = values.get(i);
                    }

                    replacement = StringUtils.replaceEach(replacement, searchList, replacementList);

                    Component component = messagePipeline.builder(sender, receiver, replacement)
                            .flag(MessageFlag.REPLACEMENT, false)
                            .build();

                    yield Tag.selfClosingInserting(component);
                }
            };
        });

        // deprecated resolvers
        if (permissionChecker.check(sender, permission.getValues().get("ping"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PING, (argumentQueue, context) ->
                    replacementModule.pingTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("tps"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.TPS, (argumentQueue, context) ->
                    replacementModule.tpsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("online"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ONLINE, (argumentQueue, context) ->
                    replacementModule.onlineTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("coords"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.COORDS, (argumentQueue, context) ->
                    replacementModule.coordsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("stats"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STATS, (argumentQueue, context) ->
                    replacementModule.statsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("skin"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SKIN, (argumentQueue, context) ->
                    replacementModule.skinTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("item"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ITEM, (argumentQueue, context) ->
                    replacementModule.itemTag(sender, receiver, isTranslateItem)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("url"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.URL, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return replacementModule.urlTag(sender, receiver, argument.value());
            });
        }

        if (permissionChecker.check(sender, permission.getValues().get("image"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.IMAGE, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return replacementModule.imageTag(sender, receiver, argument.value());
            });
        }

        if (permissionChecker.check(sender, permission.getValues().get("spoiler"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SPOILER, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return replacementModule.spoilerTag(sender, receiver, argument.value(), messageContext.getFlags());
            });
        }
    }

}
