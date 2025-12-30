package net.flectone.pulse.module.message;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.auto.AutoModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.bossbar.BossbarModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.module.message.update.UpdateModule;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                AfkModule.class,
                AnvilModule.class,
                AutoModule.class,
                BookModule.class,
                BossbarModule.class,
                BrandModule.class,
                BubbleModule.class,
                ChatModule.class,
                FormatModule.class,
                GreetingModule.class,
                JoinModule.class,
                ObjectiveModule.class,
                QuitModule.class,
                RightclickModule.class,
                SidebarModule.class,
                SignModule.class,
                StatusModule.class,
                TabModule.class,
                UpdateModule.class,
                VanillaModule.class
        );
    }

    @Override
    public Message config() {
        return fileFacade.message();
    }

    @Override
    public Permission.Message permission() {
        return fileFacade.permission().message();
    }

}
