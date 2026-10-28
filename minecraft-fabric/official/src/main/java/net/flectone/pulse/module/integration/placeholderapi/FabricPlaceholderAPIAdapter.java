package net.flectone.pulse.module.integration.placeholderapi;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

public interface FabricPlaceholderAPIAdapter {

    String parse(String message, ServerPlayer player);

    void registerCommon(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler);

    void registerServer(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler);

}
