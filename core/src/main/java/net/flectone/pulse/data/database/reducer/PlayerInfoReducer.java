package net.flectone.pulse.data.database.reducer;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.util.constant.SettingText;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.EnumMap;
import java.util.Map;

public class PlayerInfoReducer implements LinkedHashMapRowReducer<Integer, FPlayerDAO.PlayerInfo> {

    @Override
    public void accumulate(Map<Integer, FPlayerDAO.PlayerInfo> container, RowView rowView) {
        int id = rowView.getColumn("id", Integer.class);
        FPlayerDAO.PlayerInfo playerInfo = container.computeIfAbsent(id, k -> {
            boolean online = rowView.getColumn("online", Boolean.class);
            String uuid = rowView.getColumn("uuid", String.class);
            String name = rowView.getColumn("name", String.class);
            String ip = rowView.getColumn("ip", String.class);
            return new FPlayerDAO.PlayerInfo(id, online, uuid, name, ip, new Object2BooleanOpenHashMap<>(), new EnumMap<>(SettingText.class));
        });

        String type = rowView.getColumn("type", String.class);
        String value = rowView.getColumn("value", String.class);
        if (value != null) {

            SettingText setting = SettingText.fromString(type);
            if (setting != null) {
                playerInfo.settingsText().put(setting, value);
                return;
            }

            playerInfo.settingsBoolean().put(type.toUpperCase(), "1".equals(value));
        }
    }

}
