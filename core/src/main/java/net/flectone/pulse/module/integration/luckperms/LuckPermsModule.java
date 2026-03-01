package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.luckperms.listener.LuckPermsPulseListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LuckPermsModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final LuckPermsIntegration luckPermsIntegration;
    private final PlatformServerAdapter platformServerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        super.onEnable();

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            luckPermsIntegration.hookLater();
        } else {
            luckPermsIntegration.hook();
        }

        if (config().useWeightReplacement()) {
            listenerRegistry.register(LuckPermsPulseListener.class);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        luckPermsIntegration.unhook();
    }

    @Override
    public Integration.Luckperms config() {
        return fileFacade.integration().luckperms();
    }

    @Override
    public Permission.Integration.Luckperms permission() {
        return fileFacade.permission().integration().luckperms();
    }

    public boolean hasLuckPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        return luckPermsIntegration.hasPermission(fPlayer, permission);
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!config().tabSort()) return 0;

        return luckPermsIntegration.getGroupWeight(fPlayer);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return luckPermsIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return luckPermsIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        return luckPermsIntegration.getGroups();
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (isModuleDisabledFor(messageContext.sender())) return messageContext;
        if (!(messageContext.sender() instanceof FPlayer fPlayer)) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.WEIGHT_REPLACEMENT, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String weightReplacementName = argumentQueue.pop().lowerValue();

            String replacementValue = getReplacementValue(fPlayer, messageContext.receiver(), weightReplacementName);
            if (replacementValue == null) return MessagePipeline.ReplacementTag.emptyTag();

            MessageContext replacementContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), replacementValue)
                    .withFlags(messageContext.flags());

            return Tag.inserting(messagePipeline.build(replacementContext));
        });
    }

    @Nullable
    public String getReplacementValue(FPlayer fPlayer, String name) {
        return getReplacementValue(fPlayer, fPlayer, name);
    }

    @Nullable
    public String getReplacementValue(FPlayer fPlayer, FPlayer fReceiver, String name) {
        if (!isEnable()) return null;

        Map<Integer, String> weightReplacement = fileFacade.localization(fReceiver).integration().luckperms().weightReplacement().get(name);
        if (weightReplacement == null || weightReplacement.isEmpty()) return null;

        int playerWeight = getGroupWeight(fPlayer);

        int lastWeight = weightReplacement.keySet().stream()
                .filter(weightKey -> weightKey <= playerWeight)
                .mapToInt(weightKey -> weightKey)
                .max()
                .orElse(-1);

        return weightReplacement.get(lastWeight);
    }

}
