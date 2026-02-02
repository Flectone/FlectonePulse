package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleMessageUtil {

    public String findFirstColor(Component component) {
        TextColor textColor = findFirstColorComponent(component).color();
        return textColor == null ? "#FFFFFF" : textColor.asHexString();
    }

    public Component findFirstColorComponent(Component component) {
        if (component.color() != null) {
            return component;
        }

        for (Component child : component.children()) {
            Component colorComponent = findFirstColorComponent(child);
            if (colorComponent.color() != null) {
                return colorComponent;
            }
        }

        return Component.empty();
    }

}
