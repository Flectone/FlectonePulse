package net.flectone.pulse.module.command.emit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmitModule extends AbstractModuleCommand<Localization.Command.Emit> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final FPlayerService fPlayerService;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptType = addPrompt(1, Localization.Command.Prompt::type);
        String promptMessage = addPrompt(2, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser())
                .required(promptType, commandParserProvider.messageParser(), typeWithMessageSuggestion())
                .optional(promptMessage, commandParserProvider.messageParser()) // not used, only for better message help
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String targetName = getArgument(commandContext, 0);
        String typeWithMessage = getArgument(commandContext, 1);

        Destination destination = parseDestination(typeWithMessage);
        String message = parseMessage(destination, typeWithMessage);

        if (targetName.equalsIgnoreCase("all")) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .range(Range.get(Range.Type.PROXY))
                    .format(Localization.Command.Emit::format)
                    .message(message)
                    .destination(destination)
                    .sound(soundOrThrow())
                    .proxy(dataOutputStream -> {
                        // same format as 1 player
                        dataOutputStream.writeAsJson(fPlayerService.getConsole()); // proxy indicator
                        dataOutputStream.writeAsJson(destination);
                        dataOutputStream.writeString(message);
                    })
                    .integration()
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (!fTarget.isOnline()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Emit::nullPlayer)
                    .build()
            );

            return;
        }

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .filterPlayer(fTarget)
                .format(Localization.Command.Emit::format)
                .message(message)
                .destination(destination)
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(fTarget);
                    dataOutputStream.writeAsJson(destination);
                    dataOutputStream.writeString(message);
                })
                .integration()
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_EMIT;
    }

    @Override
    public Command.Emit config() {
        return fileFacade.command().emit();
    }

    @Override
    public Permission.Command.Emit permission() {
        return fileFacade.permission().command().emit();
    }

    @Override
    public Localization.Command.Emit localization(FEntity sender) {
        return fileFacade.localization(sender).command().emit();
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeWithMessageSuggestion() {
        return (context, input) -> {
            String[] words = input.input().split(" ");
            String string = words.length < 3 ? "" : words[2];

            int indexStartBracket = string.indexOf("{");
            int indexEndBracket = string.lastIndexOf("}");

            if (indexStartBracket == -1 && indexEndBracket == -1) {
                return Arrays.stream(Destination.Type.values())
                        .map(type -> Suggestion.suggestion(type.name()))
                        .toList();
            }

            return Collections.emptyList();
        };
    }

    private Destination parseDestination(String string) {
        try {
            int startIndexBracket = string.indexOf("{");
            if (startIndexBracket == -1) {
                String type = string.split(" ")[0];
                return Destination.fromJson(Map.of("type", type));
            }

            int endIndexBracket = findMatchingBracket(string, startIndexBracket);
            if (endIndexBracket == -1) {
                String type = string.split(" ")[0];
                return Destination.fromJson(Map.of("type", type));
            }

            String type = string.substring(0, startIndexBracket);
            Map<String, Object> destination = new HashMap<>();
            destination.put("type", type);

            String content = string.substring(startIndexBracket + 1, endIndexBracket);
            parseContent(content, destination);

            return Destination.fromJson(destination);
        } catch (Exception ignored) {
            return new Destination();
        }
    }

    private void parseContent(String content, Map<String, Object> map) {
        List<String> pairs = splitKeyValuePairs(content);

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if (value.startsWith("{") && value.endsWith("}")) {
                    Map<String, Object> nestedMap = new HashMap<>();
                    parseContent(value.substring(1, value.length() - 1), nestedMap);
                    map.put(key, nestedMap);
                } else if (value.startsWith("\"") && value.endsWith("\"")) {
                    map.put(key, value.substring(1, value.length() - 1));
                } else {
                    map.put(key, value);
                }
            }
        }
    }

    private int findMatchingBracket(String string, int startIndex) {
        int depth = 1;
        for (int i = startIndex + 1; i < string.length(); i++) {
            char symbol = string.charAt(i);
            if (symbol == '{') {
                depth++;
            }

            if (symbol == '}') {
                depth--;
            }

            if (depth == 0) {
                return i;
            }
        }

        return -1;
    }

    private List<String> splitKeyValuePairs(String content) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int depth = 0;
        for (char symbol : content.toCharArray()) {
            if (symbol == '{') {
                depth++;
            }

            if (symbol == '}') {
                depth--;
            }

            if (symbol == ',' && depth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(symbol);
            }
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }

    public String parseMessage(Destination destination, String string) {
        int startIndexBracket = string.indexOf("{");
        if (startIndexBracket == -1) {
            String typeName = destination.getType().name();
            if (string.startsWith(typeName + " ")) {
                return string.substring(typeName.length()).trim();
            }

            return string;
        }

        int endIndexBracket = findMatchingBracket(string, startIndexBracket);
        if (endIndexBracket == -1 || endIndexBracket == string.length() - 1) {
            return "";
        }

        return string.substring(endIndexBracket + 1).trim();
    }
}