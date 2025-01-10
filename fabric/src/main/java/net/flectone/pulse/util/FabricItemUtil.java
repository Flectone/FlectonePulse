package net.flectone.pulse.util;

import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;

@Singleton
public class FabricItemUtil implements ItemUtil {

    @Override
    public Component translatableComponent(Object item) {
        return Component.empty();
    }

    @Override
    public HoverEvent<?> hoverEvent(Object item) {
        return null;
    }

    @Override
    public void decreaseItemAmount(Object item, Runnable replaceItem) {

    }

    @Override
    public void dropItem(Object location, Object itemStack) {

    }

    @Override
    public void removeSignIndex(Object itemMeta) {

    }

    @Override
    public void setSignIndex(Object itemMeta, int[] signIndexes) {

    }

    @Override
    public Pair<Integer, int[]> findSignIndex(Object itemMeta, List<String> itemLore, FPlayer fPlayer) {
        return null;
    }

}
