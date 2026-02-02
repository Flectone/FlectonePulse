package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.deletemessage.DeletemessageModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.sprite.SpriteModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.bossbar.BossbarModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleBasePulseListener implements PulseListener {

    private static final List<Class<? extends AbstractModule>> NOT_SUPPORTED_MODULES = List.of(
            AnvilModule.class,
            BookModule.class,
            BossbarModule.class,
            ObjectModule.class,
            ObjectiveModule.class,
            RightclickModule.class,
            SignModule.class,
            StatusModule.class,
            TabModule.class,
            DeleteModule.class,
            DeletemessageModule.class,
            RockpaperscissorsModule.class,
            SpriteModule.class,
            TictactoeModule.class,
            OnlineModule.class,
            ToponlineModule.class
    );

    private final FLogger fLogger;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        AbstractModule eventModule = event.module();
        if (NOT_SUPPORTED_MODULES.stream().anyMatch(clazz -> clazz.isInstance(eventModule))) {
            fLogger.warning(eventModule.getClass().getSimpleName() + " is not supported on Hytale");
            return event.withCancelled(true);
        }

        return event;
    }

}
