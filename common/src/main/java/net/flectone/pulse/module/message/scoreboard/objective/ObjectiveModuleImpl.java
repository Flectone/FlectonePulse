package net.flectone.pulse.module.message.scoreboard.objective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.TabnameModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectiveModuleImpl implements ObjectiveModule {

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(ObjectiveModule.super.children());
        children.add(BelownameModule.class);
        children.add(TabnameModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public void createObjective(FPlayer fPlayer, Component displayName, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        // scoreboards are a platform feature, nothing to do by default
    }

    @Override
    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        // scoreboards are a platform feature, nothing to do by default
    }

    @Override
    public void removeObjective(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        // scoreboards are a platform feature, nothing to do by default
    }

    @Override
    public void forgetScore(FPlayer fPlayer, FPlayer fObjective, ScoreboardPosition scoreboardPosition) {
        // scoreboards are a platform feature, nothing to do by default
    }

    @Override
    public Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format) {
        return buildFormat(fPlayer, fReceiver, score, format, true);
    }

    @Override
    public Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format, boolean colorContextSender) {
        return messagePipeline.build(StringMessageContext.builder()
                .base(MessageContext.builder()
                        .module(this.name())
                        .sender(fPlayer)
                        .receiver(fReceiver)
                        .message(format)
                        .flag(MessageFlag.COLOR_CONTEXT_SENDER, colorContextSender)
                        .tagResolver(messagePipeline.resolver("score", (_, _) ->
                                Tag.inserting(messagePipeline.build(MessageContext.builder()
                                                .module(this.name())
                                                .sender(fPlayer)
                                                .receiver(fReceiver)
                                                .message(score)
                                                .flag(MessageFlag.COLOR_CONTEXT_SENDER, colorContextSender)
                                                .build()
                                        )
                                )
                        ))
                        .build()
                )
                .string(score)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SCOREBOARD_OBJECTIVE;
    }

    @Override
    public Message.Scoreboard.Objective config() {
        return fileFacade.message().scoreboard().objective();
    }

    @Override
    public Permission.Message.Scoreboard.Objective permission() {
        return fileFacade.permission().message().scoreboard().objective();
    }

}
