package net.flectone.pulse.module.command.minesweeper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.minesweeper.listener.MinesweeperPulseListener;
import net.flectone.pulse.module.command.minesweeper.model.Minesweeper;
import net.flectone.pulse.module.command.minesweeper.model.MinesweeperMessageContext;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinesweeperModuleImpl implements MinesweeperModule {

    private final Map<UUID, Minesweeper> playerGames = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;
    private final RandomGenerator randomGenerator;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptNumber = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptNumber + " " + 1, commandParserProvider.integerParser(0, config().maxRow()), DefaultValue.constant(-1))
                .optional(promptNumber + " " + 2, commandParserProvider.integerParser(0, config().maxColumn()), DefaultValue.constant(-1))
                .optional(promptNumber + " " + 3, commandParserProvider.integerParser(0, config().maxRow() * config().maxColumn() - Math.min(9, config().maxRow() * config().maxColumn())), DefaultValue.constant(config().defaultMine()))
                .optional(promptNumber + " " + 4, commandParserProvider.integerParser(), DefaultValue.constant(randomGenerator.nextInt(Integer.MAX_VALUE)))
        );

        listenerRegistry.register(MinesweeperPulseListener.class);
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);

        playerGames.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String type = commandModuleController.getArgument(this, commandContext, 0);
        if (type.equalsIgnoreCase("create")) {
            create(fPlayer, commandContext);
            return;
        }

        Minesweeper minesweeper = playerGames.get(fPlayer.uuid());
        if (minesweeper == null || minesweeper.getState() != Minesweeper.GameState.IN_PROGRESS) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongGame())
                            .build()
                    )
                    .build()
            );

            return;
        }

        String promptNumber = commandModuleController.getPrompt(this, 1);
        int optionalRow = commandContext.get(promptNumber + " " + 1);
        int optionalColumn = commandContext.get(promptNumber + " " + 2);

        boolean isFlagCommand = type.equalsIgnoreCase("flag");
        if (optionalRow == -1 && optionalColumn == -1 && isFlagCommand) {
            minesweeper.setFlagMode(!minesweeper.isFlagMode());
            sendMessage(fPlayer, minesweeper, config().maxRow(), config().maxColumn(), Localization.Command.Minesweeper::formatMove);
            return;
        }

        int row = optionalRow == -1 ? config().maxRow() : optionalRow;
        int column = optionalColumn == -1 ? config().maxColumn() : optionalColumn;

        if (!minesweeper.checkBounds(row, column)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongMove())
                            .build()
                    )
                    .build()
            );
            return;
        }

        if (isFlagCommand || minesweeper.isFlagMode()) {
            flag(fPlayer, minesweeper, row, column);
        } else {
            move(fPlayer, minesweeper, row, column);
        }
    }

    @Override
    public void removeGame(UUID uuid) {
        playerGames.remove(uuid);
    }

    private void create(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (config().checkDuplicate() && playerGames.containsKey(fPlayer.uuid())) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).alreadyInGame())
                            .build()
                    )
                    .build()
            );
            return;
        }

        String promptNumber = commandModuleController.getPrompt(this, 1);
        int optionalRow = commandContext.get(promptNumber + " " + 1);
        int optionalColumn = commandContext.get(promptNumber + " " + 2);

        int rowCount = optionalRow == -1 ? config().maxRow() : optionalRow;
        int columnCount = optionalColumn == -1 ? config().maxColumn() : optionalColumn;
        int mineCount = commandContext.get(promptNumber + " " + 3);

        if (rowCount <= 0
                || columnCount <= 0
                || mineCount < 0
                || mineCount > rowCount * columnCount - Math.min(9, rowCount * columnCount)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongParameters())
                            .build()
                    )
                    .build()
            );
            return;
        }

        int seed = commandContext.get(promptNumber + " " + 4);
        Minesweeper minesweeper = new Minesweeper(rowCount, columnCount, mineCount, seed);

        playerGames.put(fPlayer.uuid(), minesweeper);

        sendMessage(fPlayer, minesweeper, rowCount, columnCount, Localization.Command.Minesweeper::formatStart);
    }

    private void flag(FPlayer fPlayer, Minesweeper minesweeper, int row, int column) {
        minesweeper.toggleFlag(row, column);
        sendMessage(fPlayer, minesweeper, row, column, Localization.Command.Minesweeper::formatMove);
    }

    private void move(FPlayer fPlayer, Minesweeper minesweeper, int row, int column) {
        Minesweeper.RevealResult result = minesweeper.reveal(row, column);
        if (result.hitMine()) {
            minesweeper.revealAll(true);
            sendMessage(fPlayer, minesweeper, row, column, Localization.Command.Minesweeper::formatLose);
            removeGame(fPlayer.uuid());
            return;
        }

        if (result.state() == Minesweeper.GameState.WIN) {
            minesweeper.revealAll(false);
            sendMessage(fPlayer, minesweeper, row, column, Localization.Command.Minesweeper::formatWin);
            removeGame(fPlayer.uuid());
            return;
        }

        if (minesweeper.getFlagged()[row][column]) {
            flag(fPlayer, minesweeper, row, column);
            return;
        }

        if (result.cellsOpened() != 0) {
            sendMessage(fPlayer, minesweeper, row, column, Localization.Command.Minesweeper::formatMove);
        }
    }

    private void sendMessage(FPlayer fPlayer,
                             Minesweeper minesweeper,
                             int row,
                             int column,
                             Function<Localization.Command.Minesweeper, String> localizationFunction) {
        messageDispatcher.dispatch(this, EventMetadata.builder()
                .sound(soundOrThrow())
                .messageContext(fResolver -> {
                    Localization.Command.Minesweeper localization = localization(fResolver);

                    String seed = StringUtils.replaceEach(
                            localization.seed(),
                            new String[]{"<row>", "<column>", "<seed>"},
                            new String[]{String.valueOf(row), String.valueOf(column), String.valueOf(minesweeper.getSeed())}
                    );
                    String remaining = String.valueOf(minesweeper.getMineCount() - minesweeper.getFlaggedCellCount());
                    String flag = Strings.CS.replace(
                            minesweeper.isFlagMode() ? localization.flagDisabled() : localization.flagEnabled(),
                            "<command>",
                            commandModuleController.getCommandName(this)
                    );

                    return MinesweeperMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(fPlayer)
                                    .receiver(fResolver)
                                    .message(StringUtils.replaceEach(
                                            localizationFunction.apply(localization),
                                            new String[]{"<seed>", "<remaining>", "<flag>", "<field>"},
                                            new String[]{seed, remaining, flag, render(minesweeper, localization)}
                                    ))
                                    .build()
                            )
                            .minesweeper(minesweeper)
                            .build();
                })
                .build()
        );
    }

    private String render(Minesweeper minesweeper, Localization.Command.Minesweeper localization) {
        Localization.Command.Minesweeper.Cell localizationCell = localization.cell();

        return minesweeper.render(localization.line(), cell -> {
            if (cell.flagged()) return replaceCommandPlaceholder(cell, localizationCell.flag());
            if (!cell.revealed()) return replaceCommandPlaceholder(cell, localizationCell.unknown());
            if (cell.mine()) return localizationCell.mine();
            if (cell.isEmpty()) return localizationCell.number0();

            return switch (cell.adjacentMines()) {
                case 1 -> localizationCell.number1();
                case 2 -> localizationCell.number2();
                case 3 -> localizationCell.number3();
                case 4 -> localizationCell.number4();
                case 5 -> localizationCell.number5();
                case 6 -> localizationCell.number6();
                case 7 -> localizationCell.number7();
                case 8 -> localizationCell.number8();
                default -> "Unknown";
            };
        });
    }

    private String replaceCommandPlaceholder(Minesweeper.CellView cell, String string) {
        return StringUtils.replaceEach(
                string,
                new String[]{"<command>", "<row>", "<column>"},
                new String[]{commandModuleController.getCommandName(this), String.valueOf(cell.row()), String.valueOf(cell.column())}
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_MINESWEEPER;
    }

    @Override
    public Command.Minesweeper config() {
        return fileFacade.command().minesweeper();
    }

    @Override
    public Permission.Command.Minesweeper permission() {
        return fileFacade.permission().command().minesweeper();
    }

    @Override
    public Localization.Command.Minesweeper localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().minesweeper();
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (_, _) -> List.of(
                Suggestion.suggestion("create"),
                Suggestion.suggestion("flag"),
                Suggestion.suggestion("move")
        );
    }

}
