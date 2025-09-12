package net.flectone.pulse.module.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.attribute.AttributeModule;
import net.flectone.pulse.module.message.auto.AutoModule;
import net.flectone.pulse.module.message.bed.BedModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.clear.ClearModule;
import net.flectone.pulse.module.message.clone.CloneModule;
import net.flectone.pulse.module.message.commandblock.CommandblockModule;
import net.flectone.pulse.module.message.damage.DamageModule;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.debugstick.DebugstickModule;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.flectone.pulse.module.message.difficulty.DifficultyModule;
import net.flectone.pulse.module.message.effect.EffectModule;
import net.flectone.pulse.module.message.enchant.EnchantModule;
import net.flectone.pulse.module.message.execute.ExecuteModule;
import net.flectone.pulse.module.message.experience.ExperienceModule;
import net.flectone.pulse.module.message.fill.FillModule;
import net.flectone.pulse.module.message.fillbiome.FillbiomeModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.flectone.pulse.module.message.gamerule.GameruleModule;
import net.flectone.pulse.module.message.give.GiveModule;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.kill.KillModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.op.OpModule;
import net.flectone.pulse.module.message.particle.ParticleModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.reload.ReloadModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.rotate.RotateModule;
import net.flectone.pulse.module.message.save.SaveModule;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.flectone.pulse.module.message.setblock.SetblockModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.sleep.SleepModule;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.stop.StopModule;
import net.flectone.pulse.module.message.summon.SummonModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.module.message.teleport.TeleportModule;
import net.flectone.pulse.module.message.time.TimeModule;
import net.flectone.pulse.module.message.update.UpdateModule;
import net.flectone.pulse.module.message.weather.WeatherModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MessageModule extends AbstractModule {

    private final Message message;
    private final Permission.Message permission;

    @Inject
    public MessageModule(FileResolver fileResolver) {
        this.message = fileResolver.getMessage();
        this.permission = fileResolver.getPermission().getMessage();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        addChildren(AdvancementModule.class);
        addChildren(AfkModule.class);
        addChildren(AnvilModule.class);
        addChildren(AttributeModule.class);
        addChildren(AutoModule.class);
        addChildren(BedModule.class);
        addChildren(BookModule.class);
        addChildren(BrandModule.class);
        addChildren(BubbleModule.class);
        addChildren(ChatModule.class);
        addChildren(ClearModule.class);
        addChildren(CloneModule.class);
        addChildren(CommandblockModule.class);
        addChildren(DamageModule.class);
        addChildren(DeathModule.class);
        addChildren(DebugstickModule.class);
        addChildren(DeopModule.class);
        addChildren(DifficultyModule.class);
        addChildren(EffectModule.class);
        addChildren(EnchantModule.class);
        addChildren(ExecuteModule.class);
        addChildren(ExperienceModule.class);
        addChildren(FillModule.class);
        addChildren(FillbiomeModule.class);
        addChildren(FormatModule.class);
        addChildren(GamemodeModule.class);
        addChildren(GameruleModule.class);
        addChildren(GiveModule.class);
        addChildren(GreetingModule.class);
        addChildren(JoinModule.class);
        addChildren(KillModule.class);
        addChildren(ObjectiveModule.class);
        addChildren(OpModule.class);
        addChildren(ParticleModule.class);
        addChildren(QuitModule.class);
        addChildren(ReloadModule.class);
        addChildren(RightclickModule.class);
        addChildren(RotateModule.class);
        addChildren(SaveModule.class);
        addChildren(SidebarModule.class);
        addChildren(SeedModule.class);
        addChildren(SetblockModule.class);
        addChildren(SignModule.class);
        addChildren(SleepModule.class);
        addChildren(SpawnModule.class);
        addChildren(StatusModule.class);
        addChildren(StopModule.class);
        addChildren(SummonModule.class);
        addChildren(TabModule.class);
        addChildren(TeleportModule.class);
        addChildren(TimeModule.class);
        addChildren(UpdateModule.class);
        addChildren(WeatherModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
