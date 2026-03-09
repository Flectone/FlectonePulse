package net.flectone.pulse.module.message.format.condition;

import com.google.common.collect.ImmutableList;
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
import net.flectone.pulse.module.message.format.condition.listener.ConditionPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConditionModule implements ModuleLocalization<Localization.Message.Format.Condition> {

    private final FileFacade fileFacade;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        listenerRegistry.register(ConditionPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().addAll(permission().values().values());
    }

    @Override
    public Localization.Message.Format.Condition localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().condition();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_CONDITION;
    }

    @Override
    public Message.Format.Condition config() {
        return fileFacade.message().format().condition();
    }

    @Override
    public Permission.Message.Format.Condition permission() {
        return fileFacade.permission().message().format().condition();
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (moduleController.isDisabledFor(this, messageContext.sender())) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.CONDITION, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String conditionName = argumentQueue.pop().lowerValue();

            String conditionValue = getConditionValue(conditionName, messageContext.sender(), messageContext.receiver(), messageContext.flags());
            if (StringUtils.isEmpty(conditionValue)) return MessagePipeline.ReplacementTag.emptyTag();

            MessageContext conditionValueContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), conditionValue)
                    .withFlags(messageContext.flags());

            return Tag.inserting(messagePipeline.build(conditionValueContext));
        });
    }

    @Nullable
    public String getConditionValue(String conditionName, FPlayer fPlayer) {
        return getConditionValue(conditionName, fPlayer, fPlayer, Collections.emptyMap());
    }

    @Nullable
    public String getConditionValue(String conditionName, FEntity fPlayer, FPlayer fReceiver, Map<MessageFlag, Boolean> flags) {
        Message.Format.Condition.Criteria criteria = config().values().get(conditionName);
        if (criteria == null) return null;
        if (!permissionChecker.check(fPlayer, permission().values().get(conditionName))) return null;

        Map<String, String> values = localization(fReceiver).values().get(conditionName);
        if (values == null || values.isEmpty()) return null;

        String conditionValue = switch (criteria.type()) {
            case NUMBER -> {
                try {
                    double number = Double.parseDouble(buildCriteriaString(fPlayer, fReceiver, criteria.value(), flags));

                    String key = null;
                    double maxNumber = Double.MIN_VALUE;

                    for (String configKey : values.keySet()) {
                        double configNumber = Double.parseDouble(configKey);

                        if (number >= configNumber && configNumber > maxNumber) {
                            key = configKey;
                            maxNumber = configNumber;

                            // take first max number
                            if (maxNumber == number) {
                                break;
                            }
                        }
                    }

                    yield key != null ? values.get(key) : null;
                } catch (NumberFormatException ignored) {
                    yield null;
                }
            }
            case STRING -> values.get(buildCriteriaString(fPlayer, fReceiver, criteria.value(), flags).toLowerCase());
            case PERMISSION -> {
                boolean hasPermission = permissionChecker.check(fPlayer, criteria.value());
                yield values.get(String.valueOf(hasPermission));
            }
        };

        return conditionValue != null ? conditionValue : values.get("default");
    }

    private String buildCriteriaString(FEntity fPlayer, FPlayer fReceiver, String value, Map<MessageFlag, Boolean> flags) {
        MessageContext conditionNameContext = messagePipeline.createContext(fPlayer, fReceiver, value)
                .withFlags(flags);

        return messagePipeline.buildPlain(conditionNameContext).trim();
    }

}
