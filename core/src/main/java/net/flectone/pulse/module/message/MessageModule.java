package net.flectone.pulse.module.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.auto.AutoModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.clear.ClearModule;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.mark.MarkModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.op.OpModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.flectone.pulse.module.message.setblock.SetblockModule;
import net.flectone.pulse.module.message.setspawn.SetspawnModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.spawnpoint.SpawnpointModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.tab.TabModule;

@Singleton
public class MessageModule extends AbstractModule {

    private final Message message;
    private final Permission.Message permission;

    @Inject
    public MessageModule(FileManager fileManager) {
        message = fileManager.getMessage();
        permission = fileManager.getPermission().getMessage();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        addChildren(AdvancementModule.class);
        addChildren(AfkModule.class);
        addChildren(AnvilModule.class);
        addChildren(AutoModule.class);
        addChildren(BookModule.class);
        addChildren(BrandModule.class);
        addChildren(BubbleModule.class);
        addChildren(ChatModule.class);
        addChildren(ClearModule.class);
        addChildren(DeathModule.class);
        addChildren(DeopModule.class);
        addChildren(EnchantModule.class);
        addChildren(FormatModule.class);
        addChildren(GamemodeModule.class);
        addChildren(GreetingModule.class);
        addChildren(JoinModule.class);
        addChildren(MarkModule.class);
        addChildren(ObjectiveModule.class);
        addChildren(OpModule.class);
        addChildren(QuitModule.class);
        addChildren(RightclickModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(SeedModule.class);
        addChildren(SetblockModule.class);
        addChildren(SetspawnModule.class);
        addChildren(SignModule.class);
        addChildren(SpawnpointModule.class);
        addChildren(StatusModule.class);
        addChildren(TabModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

}
