package net.flectone.pulse.module.command.translateto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.FCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class BukkitTranslatetoModule extends TranslatetoModule {

    @Inject
    public BukkitTranslatetoModule(FileManager fileManager,
                                   CommandUtil commandUtil,
                                   IntegrationModule integrationModule) {
        super(fileManager, commandUtil, integrationModule);

    }

    @Override
    public void createCommand() {
        String promptMessage = getPrompt().getMessage();
        String[] suggestionLanguages = getCommand().getLanguages().toArray(new String[0]);

        new FCommand(getName(getCommand()))
                .withAliases(getCommand().getAliases())
                .withPermission(getPermission())
                .then(new StringArgument("mainLang")
                        .includeSuggestions(ArgumentSuggestions.strings(suggestionLanguages))
                        .then(new StringArgument("targetLang")
                                .includeSuggestions(ArgumentSuggestions.strings(suggestionLanguages))
                                .then(new GreedyStringArgument(promptMessage)
                                        .executes(this::executesFPlayer)
                                )
                        )
                )
                .override();
    }
}
