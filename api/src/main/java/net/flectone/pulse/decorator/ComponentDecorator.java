package net.flectone.pulse.decorator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A utility class for decorating Adventure {@link Component} objects with hover events and text decorations.
 * This decorator recursively applies decorations to components, including their children and translation arguments.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface ComponentDecorator {

    /**
     * Adds a hover event to the given component.
     * This method will overwrite any existing hover event on the component.
     *
     * @param component the component to add the hover event to
     * @param hoverEvent the hover event to apply
     * @return a new component with the hover event applied
     */
    Component hover(Component component, HoverEvent<?> hoverEvent);

    /**
     * Adds a hover event to the given component only if no hover event is already present.
     *
     * @param component the component to add the hover event to
     * @param hoverEvent the hover event to apply
     * @return a new component with the hover event applied if it was absent
     */
    Component hoverIfAbsent(Component component, HoverEvent<?> hoverEvent);

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
    Component hover(Component component, HoverEvent<?> hoverEvent, boolean ifAbsent);

    /**
     * Applies a text decoration to the given component.
     * This method will overwrite any existing state of the specified decoration.
     *
     * @param component the component to decorate
     * @param decoration the text decoration to apply (e.g., BOLD, ITALIC, UNDERLINED)
     * @param state the state of the decoration (TRUE, FALSE, or NOT_SET)
     * @return a new component with the text decoration applied
     */
    Component decorate(Component component, TextDecoration decoration, TextDecoration.State state);

    /**
     * Applies a text decoration to the given component only if that decoration is not already set.
     *
     * @param component the component to decorate
     * @param decoration the text decoration to apply (e.g., BOLD, ITALIC, UNDERLINED)
     * @param state the state of the decoration (TRUE, FALSE, or NOT_SET)
     * @return a new component with the text decoration applied if it was absent
     */
    Component decorateIfAbsent(Component component, TextDecoration decoration, TextDecoration.State state);

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
    Component decorate(Component component, TextDecoration decoration, TextDecoration.State state, boolean ifAbsent);

}
