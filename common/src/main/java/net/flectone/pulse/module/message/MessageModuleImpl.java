package net.flectone.pulse.module.message;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
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
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.module.message.update.UpdateModule;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageModuleImpl implements MessageModule {

    private final FileFacade fileFacade;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(MessageModule.super.children());
        children.add(AfkModule.class);
        children.add(AnvilModule.class);
        children.add(AutoModule.class);
        children.add(BookModule.class);
        children.add(BossbarModule.class);
        children.add(BrandModule.class);
        children.add(BubbleModule.class);
        children.add(ChatModule.class);
        children.add(FormatModule.class);
        children.add(GreetingModule.class);
        children.add(JoinModule.class);
        children.add(QuitModule.class);
        children.add(RightclickModule.class);
        children.add(ScoreboardModule.class);
        children.add(SidebarModule.class);
        children.add(SignModule.class);
        children.add(StatusModule.class);
        children.add(TabModule.class);
        children.add(UpdateModule.class);
        children.add(VanillaModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE;
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
