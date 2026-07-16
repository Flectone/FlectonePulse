package net.flectone.pulse.module.message.format.padding;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.format.padding.listener.PulsePaddingListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaddingModule implements ModuleLocalization<Localization.Message.Format.Padding> {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final SocialService socialService;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulsePaddingListener.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().addAll(permission().values().values());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_PADDING;
    }

    @Override
    public Message.Format.Padding config() {
        return fileFacade.message().format().padding();
    }

    @Override
    public Permission.Message.Format.Padding permission() {
        return fileFacade.permission().message().format().padding();
    }

    @Override
    public Localization.Message.Format.Padding localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().padding();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.PADDING.getTagName(), (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            // <padding:10:text>
            // <padding:10:text:start>
            // <padding:10:text:start:end>
            Optional<Tag> tag = createPaddingNumberTag(argumentQueue, messageContext, Message.Format.Padding.Type.LEFT);
            if (tag.isPresent()) return tag.get();

            // reset pointer
            argumentQueue.reset();

            // get first argument
            String paddingName = argumentQueue.pop().value();

            // <padding:CENTER:10:text>
            // <padding:CENTER:10:text:start>
            // <padding:CENTER:10:text:start:end>
            Message.Format.Padding.Value paddingValue = config().values().get(paddingName);
            if (paddingValue == null) {
                return Arrays.stream(Message.Format.Padding.Type.values())
                        .filter(paddingType -> paddingType.name().equalsIgnoreCase(paddingName))
                        .findAny()
                        .map(value -> createPaddingNumberTag(argumentQueue, messageContext, value)
                                .orElseGet(() -> Tag.inserting(Component.text(paddingName)))
                        )
                        .orElseGet(() -> Tag.inserting(Component.text(paddingName)));
            }

            // <padding:name:text>
            // <padding:name:text:start>
            // <padding:name:text:start:end>
            String paddingText = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";

            Localization.Message.Format.Padding.Value paddingLocalization = localization(messageContext.receiver()).values().get(paddingName);
            if (paddingLocalization == null) {
                String start = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";
                String end = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";
                paddingLocalization = new Localization.Message.Format.Padding.Value(" ", start, end);
            }

            return createPaddingTag(messageContext, paddingName, paddingText, paddingValue, paddingLocalization);
        }));
    }

    private Optional<Tag> createPaddingNumberTag(@NonNull ArgumentQueue argumentQueue,
                                                 @NonNull MessageContext messageContext,
                                                 Message.Format.Padding.@NonNull Type type) {
        if (!argumentQueue.hasNext()) return Optional.empty();

        OptionalInt paddingLength = argumentQueue.pop().asInt();
        if (paddingLength.isEmpty()) return Optional.empty();

        String paddingName = paddingLength + "_" + type.name().toLowerCase();
        String paddingText = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";
        String start = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";
        String end = argumentQueue.hasNext() ? argumentQueue.pop().value() : "";

        return Optional.of(createPaddingTag(messageContext, paddingName, paddingText,
                new Message.Format.Padding.Value(type, paddingLength.getAsInt()),
                new Localization.Message.Format.Padding.Value(" ", start, end)
        ));
    }

    @NonNull
    private Tag createPaddingTag(@NonNull MessageContext messageContext,
                                 @NonNull String paddingName,
                                 @NonNull String paddingText,
                                 Message.Format.Padding.@NonNull Value paddingValue,
                                 Localization.Message.Format.Padding.@NonNull Value paddingLocalization) {
        if (!messageContext.isFlag(MessageFlag.PADDING_MODULE)) return Tag.preProcessParsed(paddingText);
        if (!permissionChecker.check(messageContext.receiver(), permission().values().get(paddingName))) return MessagePipeline.ReplacementTag.emptyTag();

        String symbol = paddingLocalization.symbol() != null ? paddingLocalization.symbol() : " ";
        String start = paddingLocalization.start() != null ? paddingLocalization.start() : "";
        String end = paddingLocalization.end() != null ? paddingLocalization.end() : "";

        int count = paddingValue.length() != null ? Math.max(0, paddingValue.length()) : 0;
        Message.Format.Padding.Type type = paddingValue.type() != null ? paddingValue.type() : Message.Format.Padding.Type.CENTER;

        Pair<Integer, Integer> paddingCount = calculatePadding(type, count);
        String leftSymbols = symbol.repeat(paddingCount.getLeft());
        String rightSymbols = symbol.repeat(paddingCount.getRight());

        return Tag.inserting(messagePipeline.build(MessageContext.builder()
                .sender(messageContext.sender())
                .receiver(messageContext.receiver())
                .message(start + leftSymbols + paddingText + rightSymbols + end)
                .flags(messageContext.flags())
                .flag(MessageFlag.PLAYER_MESSAGE, false)
                .build()
        ));
    }

    @NonNull
    private Pair<Integer, Integer> calculatePadding(Message.Format.Padding.@NonNull Type type, int count) {
        return switch (type) {
            case LEFT -> Pair.of(count, 0);
            case RIGHT -> Pair.of(0, count);
            case CENTER, CENTER_LEFT, CENTER_RIGHT -> {
                int leftCount = count / 2;
                int rightCount = count - leftCount;

                // decide what to do with the extra symbol repetition
                if (count % 2 != 0) {
                    if (type == Message.Format.Padding.Type.CENTER_RIGHT) {
                        rightCount++;
                        leftCount = count - rightCount;
                    } else {
                        // CENTER and CENTER_LEFT behave in the same way
                        leftCount++;
                        rightCount = count - leftCount;
                    }
                }

                yield Pair.of(leftCount, rightCount);
            }
        };
    }

}
