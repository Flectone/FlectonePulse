package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NonNull;

import java.util.List;

final class ColorTransform {

    private ColorTransform() {
    }

    static int sizeOf(@NonNull List<Component> components) {
        int size = 0;
        for (Component component : components) {
            size += sizeOf(component);
        }

        return size;
    }

    static int sizeOf(@NonNull Component component) {
        int size = 0;

        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            if (!content.isEmpty()) {
                size += content.codePointCount(0, content.length());
            }
        } else {
            size++;
        }

        List<Component> children = component.children();
        for (int i = 0; i < children.size(); i++) {
            size += sizeOf(children.get(i));
        }

        return size;
    }

    static @NonNull Component apply(@NonNull Component component, @NonNull Colorizer colorizer) {
        if (component.style().color() != null) {
            advanceTree(component, colorizer);
            return component;
        }

        if (component instanceof TextComponent textComponent) {
            return applyToText(textComponent, colorizer);
        }

        return applyToOther(component, colorizer);
    }

    private static void advanceTree(@NonNull Component component, @NonNull Colorizer colorizer) {
        if (component instanceof TextComponent textComponent && !textComponent.content().isEmpty()) {
            advanceBy(textComponent.content(), colorizer);
        }

        List<Component> children = component.children();
        for (int i = 0; i < children.size(); i++) {
            advanceTree(children.get(i), colorizer);
        }
    }

    private static @NonNull Component applyToText(@NonNull TextComponent component, @NonNull Colorizer colorizer) {
        String content = component.content();
        List<Component> children = component.children();
        if (content.isEmpty() && children.isEmpty()) return component;

        TextComponent.Builder builder = Component.text();
        Style style = component.style();

        if (!content.isEmpty()) {
            int index = 0;
            int length = content.length();
            // the colour of the next run is sampled once and carried over,
            // otherwise every position would be evaluated twice
            TextColor nextColor = colorizer.color();

            while (index < length) {
                TextColor runColor = nextColor;
                int runStart = index;

                index += Character.charCount(content.codePointAt(index));
                colorizer.advance();

                while (index < length) {
                    nextColor = colorizer.color();
                    if (!nextColor.equals(runColor)) break;

                    index += Character.charCount(content.codePointAt(index));
                    colorizer.advance();
                }

                builder.append(Component.text(content.substring(runStart, index), style.color(runColor)));
            }
        }

        for (int i = 0; i < children.size(); i++) {
            builder.append(apply(children.get(i), colorizer));
        }

        return builder.build();
    }

    private static @NonNull Component applyToOther(@NonNull Component component, @NonNull Colorizer colorizer) {
        Component colored = component.colorIfAbsent(colorizer.color());
        colorizer.advance();

        List<Component> children = component.children();
        if (children.isEmpty()) return colored;

        Component[] updated = new Component[children.size()];
        for (int i = 0; i < children.size(); i++) {
            updated[i] = apply(children.get(i), colorizer);
        }

        return colored.children(List.of(updated));
    }

    private static void advanceBy(@NonNull String content, @NonNull Colorizer colorizer) {
        int count = content.codePointCount(0, content.length());
        for (int i = 0; i < count; i++) {
            colorizer.advance();
        }
    }

}
