package net.flectone.pulse.module.command.stream;

import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class StreamModule extends AbstractModuleCommand<Localization.Command.Stream> {

    @Getter
    private final Command.Stream command;
    @Getter
    private final Permission.Command.Stream permission;

    private final ThreadManager threadManager;
    private final CommandUtil commandUtil;

    public StreamModule(FileManager fileManager,
                        ThreadManager threadManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getStream(), null);

        this.threadManager = threadManager;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getStream();
        permission = fileManager.getPermission().getCommand().getStream();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        boolean isStart = commandUtil.getFull(arguments).contains("start");
        boolean isStream = fPlayer.is(FPlayer.Setting.STREAM);

        if (isStream && isStart && !fPlayer.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Stream::getAlready)
                    .sendBuilt();
            return;
        }

        if (!isStream && !isStart) {
            builder(fPlayer)
                    .format(Localization.Command.Stream::getNot)
                    .sendBuilt();
            return;
        }

        setStreamPrefix(fPlayer, isStart);

        if (isStart) {
            String rawString = commandUtil.getString(0, arguments);

            builder(fPlayer)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageTag.COMMAND_STREAM)
                    .format(replaceUrls(rawString))
                    .proxy(output -> output.writeUTF(rawString))
                    .integration(s -> s.replace("<urls>", rawString))
                    .sound(getSound())
                    .sendBuilt();

        } else {
            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Stream::getFormatEnd)
                    .sendBuilt();
        }
    }

    public Function<Localization.Command.Stream, String> replaceUrls(String string) {
        return message -> {
            List<String> urls = Arrays.stream(string.split(" "))
                    .map(url -> message.getUrlTag().replace("<url>", url))
                    .toList();

            return message.getFormatStart()
                    .replace("<urls>", String.join("<br>", urls));
        };
    }

    @Async
    public void setStreamPrefix(FPlayer fPlayer, boolean isStart) {
        if (checkModulePredicates(fPlayer)) return;
        if (fPlayer.isUnknown()) return;

        fPlayer.set(FPlayer.Setting.STREAM, isStart);
        Localization.Command.Stream localization = resolveLocalization(fPlayer);
        fPlayer.setStreamPrefix(isStart ? localization.getPrefixTrue() : localization.getPrefixFalse());

        threadManager.runDatabase(database -> database.updateFPlayer(fPlayer));
    }

    public TagResolver streamTag(@NotNull FEntity sender) {
        if (!isEnable()) return TagResolver.empty();
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();

        return TagResolver.resolver("stream_prefix", (argumentQueue, context) -> {
            if (checkModulePredicates(fPlayer)) return Tag.selfClosingInserting(Component.empty());
            if (fPlayer.getStreamPrefix() == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(fPlayer.getStreamPrefix());
        });
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
