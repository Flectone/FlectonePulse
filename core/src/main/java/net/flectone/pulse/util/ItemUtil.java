package net.flectone.pulse.util;

import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;

public interface ItemUtil {

    Component translatableComponent(Object item);
    HoverEvent<?> hoverEvent(Object item);
    void decreaseItemAmount(Object item, Runnable replaceItem);
    void dropItem(Object location, Object itemStack);
    void removeSignIndex(Object itemMeta);
    void setSignIndex(Object itemMeta, int[] signIndexes);
    Pair<Integer, int[]> findSignIndex(Object itemMeta, List<String> itemLore, FPlayer fPlayer);
}
