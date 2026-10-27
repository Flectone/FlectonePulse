package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.flectone.pulse.BuildConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

@Singleton
public class FabricPlaceholderAPIAdapter {

    public String parse(String message, ServerPlayer player) {
        return NodeParser.builder()
                .serverPlaceholders()
                .commonPlaceholders()
                .simplifiedTextFormat()
                .build()
                .parseComponent(message, ServerPlaceholderContext.of(player).asParserContext())
                .getString();
    }

    public void registerCommon(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + name), handler::apply);
    }

    public void registerServer(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        Placeholders.registerServer(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + name), handler::apply);
    }

}
