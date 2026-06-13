package net.flectone.pulse.util.decorator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

/**
 * A utility class for decorating Adventure {@link Component} objects with hover events and text decorations.
 * This decorator recursively applies decorations to components, including their children and translation arguments.
 *
 * @author TheFaser
 * @since 1.10.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComponentDecorator {

    /**
     * Adds a hover event to the given component.
     * This method will overwrite any existing hover event on the component.
     *
     * @param component the component to add the hover event to
     * @param hoverEvent the hover event to apply
     * @return a new component with the hover event applied
     */
    public Component hover(Component component, HoverEvent<?> hoverEvent) {
        return hover(component, hoverEvent, false);
    }

    /**
     * Adds a hover event to the given component only if no hover event is already present.
     *
     * @param component the component to add the hover event to
     * @param hoverEvent the hover event to apply
     * @return a new component with the hover event applied if it was absent
     */
    public Component hoverIfAbsent(Component component, HoverEvent<?> hoverEvent) {
        return hover(component, hoverEvent, true);
    }

    /**
     * Adds a hover event to the given component with control over whether to overwrite existing hover events.
     * This method recursively processes the component's children and translation arguments to ensure
     * consistent hover behavior throughout the component tree.
     *
     * @param component the component to add the hover event to
     * @param hoverEvent the hover event to apply
     * @param ifAbsent if true, only adds the hover event if one is not already present; if false, overwrites any existing hover event
     * @return a new component with the hover event applied to it, its children, and translation arguments
     */
    public Component hover(Component component, HoverEvent<?> hoverEvent, boolean ifAbsent) {
        Component result = component.hoverEvent() == null
                ? component.hoverEvent(hoverEvent)
                : component;

        // recursively apply hover event to translation arguments if this is a translatable component
        if (result instanceof TranslatableComponent translatableComponent) {
            List<TranslationArgument> translationArguments = translatableComponent.arguments().stream()
                    .map(translationArgument -> hoverTranslationArgument(translationArgument, hoverEvent, ifAbsent))
                    .toList();

            result = translatableComponent.arguments(translationArguments);
        }

        // recursively apply hover event to all child components
        List<Component> newChildren = result.children().stream()
                .map(child -> hover(child, hoverEvent, ifAbsent))
                .toList();

        return result.children(newChildren);
    }

    private TranslationArgument hoverTranslationArgument(TranslationArgument argument, HoverEvent<?> hoverEvent, boolean ifAbsent) {
        if (argument instanceof Component component) {
            return (TranslationArgument) hover(component, hoverEvent, ifAbsent);
        }

        return argument;
    }

    /**
     * Applies a text decoration to the given component.
     * This method will overwrite any existing state of the specified decoration.
     *
     * @param component the component to decorate
     * @param decoration the text decoration to apply (e.g., BOLD, ITALIC, UNDERLINED)
     * @param state the state of the decoration (TRUE, FALSE, or NOT_SET)
     * @return a new component with the text decoration applied
     */
    public Component decorate(Component component, TextDecoration decoration, TextDecoration.State state) {
        return decorate(component, decoration, state, false);
    }

    /**
     * Applies a text decoration to the given component only if that decoration is not already set.
     *
     * @param component the component to decorate
     * @param decoration the text decoration to apply (e.g., BOLD, ITALIC, UNDERLINED)
     * @param state the state of the decoration (TRUE, FALSE, or NOT_SET)
     * @return a new component with the text decoration applied if it was absent
     */
    public Component decorateIfAbsent(Component component, TextDecoration decoration, TextDecoration.State state) {
        return decorate(component, decoration, state, true);
    }

    /**
     * Applies a text decoration to the given component with control over whether to overwrite existing decorations.
     * This method recursively processes the component's children and translation arguments to ensure
     * consistent decoration throughout the component tree.
     *
     * @param component the component to decorate
     * @param decoration the text decoration to apply (e.g., BOLD, ITALIC, UNDERLINED)
     * @param state the state of the decoration (TRUE, FALSE, or NOT_SET)
     * @param ifAbsent if true, only applies the decoration if it is not already set; if false, overwrites any existing decoration state
     * @return a new component with the text decoration applied to it, its children, and translation arguments
     */
    public Component decorate(Component component, TextDecoration decoration, TextDecoration.State state, boolean ifAbsent) {
        Component result = ifAbsent
                ? component.decorationIfAbsent(decoration, state)
                : component.decoration(decoration, state);

        // recursively apply decoration to translation arguments if this is a translatable component
        if (result instanceof TranslatableComponent translatableComponent) {
            List<TranslationArgument> translationArguments = translatableComponent.arguments().stream()
                    .map(translationArgument -> decorateTranslationArgument(translationArgument, decoration, state, ifAbsent))
                    .toList();

            result = translatableComponent.arguments(translationArguments);
        }

        // recursively apply decoration to all child components
        List<Component> newChildren = result.children().stream()
                .map(child -> decorate(child, decoration, state, ifAbsent))
                .toList();

        return result.children(newChildren);
    }

    private TranslationArgument decorateTranslationArgument(TranslationArgument argument, TextDecoration decoration, TextDecoration.State state, boolean ifAbsent) {
        if (argument instanceof Component component) {
            return (TranslationArgument) decorate(component, decoration, state, ifAbsent);
        }

        return argument;
    }

}
