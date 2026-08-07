package net.flectone.pulse.decorator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComponentDecoratorImpl implements ComponentDecorator {

    @Override
    public Component hover(Component component, HoverEvent<?> hoverEvent) {
        return hover(component, hoverEvent, false);
    }

    @Override
    public Component hoverIfAbsent(Component component, HoverEvent<?> hoverEvent) {
        return hover(component, hoverEvent, true);
    }

    @Override
    public Component hover(Component component, HoverEvent<?> hoverEvent, boolean ifAbsent) {
        Component result = component.hoverEvent() == null
                ? component.hoverEvent(hoverEvent)
                : component;

        // recursively apply hover event to translation arguments if this is a translatable component
        if (result instanceof TranslatableComponent translatableComponent) {
            List<Component> translationArguments = translatableComponent.arguments().stream()
                    .map(translationArgument -> hover(translationArgument.asComponent(), hoverEvent, ifAbsent))
                    .toList();

            result = translatableComponent.arguments(translationArguments);
        }

        // recursively apply hover event to all child components
        List<Component> newChildren = result.children().stream()
                .map(child -> hover(child, hoverEvent, ifAbsent))
                .toList();

        return result.children(newChildren);
    }

    @Override
    public Component decorate(Component component, TextDecoration decoration, TextDecoration.State state) {
        return decorate(component, decoration, state, false);
    }

    @Override
    public Component decorateIfAbsent(Component component, TextDecoration decoration, TextDecoration.State state) {
        return decorate(component, decoration, state, true);
    }

    @Override
    public Component decorate(Component component, TextDecoration decoration, TextDecoration.State state, boolean ifAbsent) {
        Component result = ifAbsent
                ? component.decorationIfAbsent(decoration, state)
                : component.decoration(decoration, state);

        // recursively apply decoration to translation arguments if this is a translatable component
        if (result instanceof TranslatableComponent translatableComponent) {
            List<Component> translationArguments = translatableComponent.arguments().stream()
                    .map(translationArgument -> decorate(translationArgument.asComponent(), decoration, state, ifAbsent))
                    .toList();

            result = translatableComponent.arguments(translationArguments);
        }

        // recursively apply decoration to all child components
        List<Component> newChildren = result.children().stream()
                .map(child -> decorate(child, decoration, state, ifAbsent))
                .toList();

        return result.children(newChildren);
    }

}
