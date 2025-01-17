package net.flectone.pulse.module.command.chatcolor;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.ColorUtil;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

@Singleton
public class BukkitChatcolorModule extends ChatcolorModule {

    private final ColorUtil colorUtil;

    @Inject
    public BukkitChatcolorModule(FileManager fileManager,
                                 ThreadManager threadManager,
                                 FPlayerManager fPlayerManager,
                                 PermissionUtil permissionUtil,
                                 ProxyManager proxyManager,
                                 CommandUtil commandUtil,
                                 ColorUtil colorUtil) {
        super(fileManager, threadManager, fPlayerManager, permissionUtil, proxyManager, commandUtil, colorUtil);

        this.colorUtil = colorUtil;
    }

    @Override
    public void createCommand() {
        String prompt = getPrompt().getColor();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(colorArgument(prompt, getColor().getValues().size())
                        .executes(this::executesFPlayer)
                )
                .override();
    }

    public Argument<String> colorArgument(String prompt, int countColors) {
        return new GreedyStringArgument(prompt).replaceSuggestions((info, builder) -> {
            String[] args = info.currentInput().split(" ");

            String current = (args.length != 1 && !info.currentInput().endsWith(" ")) ? args[args.length - 1].toLowerCase() : null;
            int currentArgLength = info.currentArg().split(" ").length;

            if (currentArgLength > countColors
                    || (current == null && currentArgLength >= countColors)) {
                return builder.buildFuture();
            }

            int builderStart = builder.getStart();
            builder = builder.createOffset(builderStart + info.currentArg().lastIndexOf(" ") + 1);

            if (current == null) {
                builder.suggest("#").suggest("&");
            } else if ((current.startsWith("#") && current.length() <= 6) ||
                    (current.startsWith("&") && current.length() == 1)) {
                builder = builder.createOffset(builderStart + info.currentArg().length());

                colorUtil.getHexSymbolList().forEach(builder::suggest);
            }

            if (currentArgLength < 2) {
                builder.suggest("clear");
            }

            // shit
            if (info.sender().hasPermission(getPermission().getOther().getName())) {
                Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .filter(name -> current == null || name != null && name.toLowerCase().startsWith(current))
                        .forEach(builder::suggest);
            }

            colorUtil.getMinecraftList().stream()
                    .filter(color -> current == null || color.startsWith(current))
                    .forEach(builder::suggest);

            return builder.buildFuture();
        });
    }


}
