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
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChild(AfkModule.class);
        addChild(AnvilModule.class);
        addChild(AutoModule.class);
        addChild(BookModule.class);
        addChild(BossbarModule.class);
        addChild(BrandModule.class);
        addChild(BubbleModule.class);
        addChild(ChatModule.class);
        addChild(FormatModule.class);
        addChild(GreetingModule.class);
        addChild(JoinModule.class);
        addChild(ObjectiveModule.class);
        addChild(QuitModule.class);
        addChild(RightclickModule.class);
        addChild(SidebarModule.class);
        addChild(SignModule.class);
        addChild(StatusModule.class);
        addChild(TabModule.class);
        addChild(UpdateModule.class);
        addChild(VanillaModule.class);
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
