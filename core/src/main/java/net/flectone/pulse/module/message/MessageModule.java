package net.flectone.pulse.module.message;

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
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageModule extends AbstractModule {

    private final FileResolver fileResolver;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChildren(AfkModule.class);
        addChildren(AnvilModule.class);
        addChildren(AutoModule.class);
        addChildren(BookModule.class);
        addChildren(BossbarModule.class);
        addChildren(BrandModule.class);
        addChildren(BubbleModule.class);
        addChildren(ChatModule.class);
        addChildren(FormatModule.class);
        addChildren(GreetingModule.class);
        addChildren(JoinModule.class);
        addChildren(ObjectiveModule.class);
        addChildren(QuitModule.class);
        addChildren(RightclickModule.class);
        addChildren(SidebarModule.class);
        addChildren(SignModule.class);
        addChildren(StatusModule.class);
        addChildren(TabModule.class);
        addChildren(UpdateModule.class);
        addChildren(VanillaModule.class);
    }

    @Override
    public Message config() {
        return fileResolver.getMessage();
    }

    @Override
    public Permission.Message permission() {
        return fileResolver.getPermission().getMessage();
    }

}
