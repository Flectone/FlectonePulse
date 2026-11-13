package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Singleton;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import net.flectone.pulse.BuildConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

@Singleton
public class FabricPlaceholderAPIAdapterV_1_19 implements FabricPlaceholderAPIAdapter {

    @Override
    public String parse(String message, ServerPlayer player) {
        return Placeholders.parseNodes(TextNode.convert(Component.literal(message)))
                .toText(ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(player)), false)
                .getString();
    }

    @Override
    public void registerCommon(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        Placeholders.register(ResourceLocation.tryParse(BuildConfig.PROJECT_MOD_ID + ":" + name), handler::apply);
    }

    @Override
    public void registerServer(String name, BiFunction<PlaceholderContext, String, PlaceholderResult> handler) {
        registerCommon(name, handler);
    }

}
