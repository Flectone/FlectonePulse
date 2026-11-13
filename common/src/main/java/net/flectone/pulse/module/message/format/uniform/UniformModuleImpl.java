package net.flectone.pulse.module.message.format.uniform;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.format.uniform.listener.PulseUniformListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UniformModuleImpl implements UniformModule {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final @Named("uniformMessage") Cache<Component, Boolean> formattedMessages;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseUniformListener.class);
    }

    @Override
    public void markFormatted(@Nullable Component component) {
        if (component == null) return;

        formattedMessages.put(component, true);
    }

    @Override
    public boolean isFormatted(@Nullable Component component) {
        return component != null && formattedMessages.getIfPresent(component) != null;
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_UNIFORM;
    }

    @Override
    public Message.Format.Uniform config() {
        return fileFacade.message().format().uniform();
    }

    @Override
    public Permission.Message.Format.Uniform permission() {
        return fileFacade.permission().message().format().uniform();
    }

}
