package net.flectone.pulse.util.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.file.FilePack;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.constant.PlatformType;
import org.apache.commons.lang3.Strings;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileMigratorImpl implements FileMigrator {

    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;

    @Override
    public FilePack migration_1_11_1(FilePack files) {
        Map<CacheName, Config.Cache.CacheSetting> cacheTypes = new LinkedHashMap<>(files.config().cache().types());
        cacheTypes.put(CacheName.MESSAGE_CONTEXT, new Config.Cache.CacheSetting(true, true, 1, TimeUnit.SECONDS, 10000));

        files = files
                .withConfig(files.config()
                        .withCache(files.config().cache()
                                .withTypes(cacheTypes)
                        )
                );

        Config.Executor configExecutor = files.config().executor();
        if (configExecutor.workQueue() == Config.Executor.WorkQueue.SYNCHRONOUS
                && configExecutor.minPoolSize() == 0
                && configExecutor.maxPoolSize() == -1) {
            files = files
                    .withConfig(files.config()
                            .withExecutor(files.config().executor()
                                    .withMinPoolSize(8)
                                    .withMaxPoolSize(8)
                                    .withWorkQueue(Config.Executor.WorkQueue.LINKED_BLOCKING)
                            )
                    );
        }

        UnaryOperator<String> replacePlayerHead = string -> Strings.CS.replace(string, "<player_head>", "<player_head_or:''>");
        UnaryOperator<String> replaceSprite = string -> Strings.CS.replace(string, "<sprite:", "<sprite_or:'':");

        Map<String, Localization> newLocalizations = new LinkedHashMap<>();

        boolean isNotHytale = platformServerAdapter.get().getPlatformType() != PlatformType.HYTALE;
        for (Localization localization : files.localizations().values()) {
            if (isNotHytale) {
                Map<String, String> newBossbarTypes = new LinkedHashMap<>();
                localization.message().bossbar().types().forEach((string, value) ->
                        newBossbarTypes.put(string, replaceSprite.apply(value))
                );

                localization = localization
                        .withMessage(localization.message()
                                .withAfk(localization.message().afk()
                                        .withFormatTrue(localization.message().afk().formatTrue()
                                                .withGlobal(replacePlayerHead.apply(localization.message().afk().formatTrue().global()))
                                        )
                                        .withFormatFalse(localization.message().afk().formatFalse()
                                                .withGlobal(replacePlayerHead.apply(localization.message().afk().formatFalse().global()))
                                        )
                                )
                                .withBossbar(localization.message().bossbar()
                                        .withTypes(newBossbarTypes)
                                )
                                .withFormat(localization.message().format()
                                        .withNames(localization.message().format().names()
                                                .withDisplay(localization.message().format().names().display().stream()
                                                        .map(replacePlayerHead)
                                                        .collect(Collectors.toCollection(LinkedList::new))
                                                )
                                                .withEntity(replaceSprite.apply(localization.message().format().names().entity()))
                                                .withConsole(Strings.CS.replace(localization.message().format().names().console(), "<player_head:", "<player_head_or:'':"))
                                        )
                                )
                                .withTab(localization.message().tab()
                                        .withPlayerlistname(localization.message().tab().playerlistname()
                                                .withFormat(replacePlayerHead.apply(localization.message().tab().playerlistname().format()))
                                        )
                                )
                        );
            }

            newLocalizations.put(localization.language(), localization);
        }

        return files.withLocalizations(newLocalizations);
    }

    @Override
    public FilePack migration_1_12_3(FilePack files) {
        Boolean usePaperMessageSender = files.config().internal().usePaperMessageSender();
        if (usePaperMessageSender != null) {
            files = files.withConfig(files.config()
                    .withInternal(files.config().internal()
                            .withUseVanillaMessageSender(usePaperMessageSender)
                            .withUsePaperMessageSender(null)
                    )
            );
        }

        Map<CacheName, Config.Cache.CacheSetting> cacheTypes = new LinkedHashMap<>(files.config().cache().types());
        cacheTypes.put(CacheName.IMAGE_PIXELS, new Config.Cache.CacheSetting(true, false, 10, TimeUnit.MINUTES, 1000));

        return files
                .withConfig(files.config()
                        .withCache(files.config().cache()
                                .withTypes(cacheTypes)
                        )
                );
    }

}