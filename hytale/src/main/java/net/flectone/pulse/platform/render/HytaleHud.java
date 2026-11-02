package net.flectone.pulse.platform.render;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.List;

public class HytaleHud extends CustomUIHud {

    public static final String KEY = "flectonepulse";

    private static final String PAGE = "HUD/FlectonePulseHud.ui";
    private static final String LINE = "HUD/FlectonePulseSidebarLine.ui";
    private static final String SIDEBAR = "#Sidebar";
    private static final String SIDEBAR_LINES = "#SidebarLines";
    private static final String ACTION_BAR = "#ActionBar";
    private static final String ACTION_BAR_TEXT = "#ActionBarText";

    private final HytaleComponentSerializer componentSerializer;

    private List<Component> sidebarLines;
    private Message.Sidebar sidebarConfig;
    private Component actionBar;
    private long actionBarId;

    public HytaleHud(PlayerRef playerRef, HytaleComponentSerializer componentSerializer) {
        super(playerRef, KEY);

        this.componentSerializer = componentSerializer;
    }

    @Override
    protected void build(UICommandBuilder commands) {
        commands.append(PAGE);

        buildSidebar(commands);
        buildActionBar(commands);
    }

    private void buildSidebar(UICommandBuilder commands) {
        if (sidebarConfig == null || sidebarLines == null || sidebarLines.isEmpty()) {
            commands.set(SIDEBAR + ".Visible", false);
            return;
        }

        commands.set(SIDEBAR + ".Visible", true);
        commands.setObject(SIDEBAR + ".Anchor", createSidebarAnchor());
        commands.setObject(SIDEBAR + ".Background", new PatchStyle().setColor(Value.of(sidebarConfig.background())));
        commands.clear(SIDEBAR_LINES);

        for (int i = 0; i < sidebarLines.size(); i++) {
            String selector = SIDEBAR_LINES + "[" + i + "]";

            commands.append(SIDEBAR_LINES, LINE);
            commands.set(selector + ".TextSpans", componentSerializer.toHytale(sidebarLines.get(i)));
            commands.set(selector + ".Padding.Left", sidebarConfig.labelLeft());
            commands.set(selector + ".Padding.Top", sidebarConfig.labelTop());
        }
    }

    private void buildActionBar(UICommandBuilder commands) {
        if (actionBar == null) {
            commands.set(ACTION_BAR + ".Visible", false);
            return;
        }

        commands.set(ACTION_BAR + ".Visible", true);
        commands.set(ACTION_BAR_TEXT + ".TextSpans", componentSerializer.toHytale(actionBar));
    }

    private Anchor createSidebarAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(sidebarConfig.width()));
        anchor.setHeight(Value.of(sidebarConfig.height()));
        anchor.setTop(Value.of(sidebarConfig.top()));
        anchor.setRight(Value.of(sidebarConfig.right()));
        return anchor;
    }

    public void setSidebar(List<Component> sidebarLines, Message.Sidebar sidebarConfig) {
        this.sidebarLines = sidebarLines;
        this.sidebarConfig = sidebarConfig;
    }

    public void clearSidebar() {
        this.sidebarLines = null;
        this.sidebarConfig = null;
    }

    public void setActionBar(Component actionBar, long actionBarId) {
        this.actionBar = actionBar;
        this.actionBarId = actionBarId;
    }

    public boolean clearActionBar(long actionBarId) {
        if (this.actionBarId != actionBarId) return false;

        this.actionBar = null;
        return true;
    }

    public boolean isEmpty() {
        return (sidebarLines == null || sidebarLines.isEmpty()) && actionBar == null;
    }

    public void flush() {
        UICommandBuilder commands = new UICommandBuilder();
        build(commands);
        update(true, commands);
    }

}
