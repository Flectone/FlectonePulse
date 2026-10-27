package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.flectone.pulse.BuildConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

@Singleton
public class FabricPlaceholderAPIAdapter {

    public String parse(String message, ServerPlayer player) {
        return Placeholders.parseText(TextParserUtils.formatText(message), PlaceholderContext.of(player)).getString();
    }

    public void registerCommon(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        Placeholders.register(Identifier.tryParse(BuildConfig.PROJECT_MOD_ID + ":" + name), handler::apply);
    }

    public void registerServer(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        registerCommon(name, handler);
    }

}
