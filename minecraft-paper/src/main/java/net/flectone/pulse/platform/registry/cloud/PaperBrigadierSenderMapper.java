package net.flectone.pulse.platform.registry.cloud;

import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.function.Function;

@RequiredArgsConstructor
public class PaperBrigadierSenderMapper<S> implements SenderMapper<S, FPlayer> {

    private final Function<S, CommandSender> senderGetter;
    private final SenderMapper<CommandSender, FPlayer> senderMapper;

    private Method getListenerMethod;

    @Override
    public @NonNull FPlayer map(@NonNull S source) {
        return senderMapper.map(senderGetter.apply(source));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull S reverse(@NonNull FPlayer mapped) {
        try {
            if (getListenerMethod == null) {
                Class<?> vanillaCommandWrapper = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".command.VanillaCommandWrapper");
                getListenerMethod = vanillaCommandWrapper.getMethod("getListener", CommandSender.class);
            }

            return (S) getListenerMethod.invoke(null, senderMapper.reverse(mapped));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get brigadier command source", e);
        }
    }

}
