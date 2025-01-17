package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitSymbolModule extends SymbolModule {

    @Inject
    public BukkitSymbolModule(FileManager fileManager,
                              CommandUtil commandUtil) {
        super(fileManager, commandUtil);
    }

    @Override
    public void createCommand() {
        String promptCategory = getPrompt().getCategory();
        String promptMessage = getPrompt().getMessage();

        Localization.Command.Symbol localization = resolveLocalization();

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new MultiLiteralArgument(promptCategory, localization.getCategories().values().toArray(new String[]{}))
                        .then(new GreedyStringArgument(promptMessage)
                                .replaceSuggestions(((info, builder) -> {
                                    builder = builder.createOffset(builder.getStart() + info.currentArg().length());

                                    String currentSymbol = (String) info.previousArgs().get(promptCategory);

                                    if (currentSymbol == null) return builder.buildFuture();

                                    var symbolCategory = localization.getCategories()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> entry.getValue().equalsIgnoreCase(currentSymbol))
                                            .findAny();

                                    if (symbolCategory.isEmpty()) return builder.buildFuture();

                                    String symbols = getCommand().getCategories().get(symbolCategory.get().getKey());
                                    for (String symbol : symbols.split(" ")) {
                                        builder.suggest(symbol);
                                    }

                                    return builder.buildFuture();
                                }))
                                .executes(this::executesFPlayer)
                        )
                )
                .override();
    }
}
