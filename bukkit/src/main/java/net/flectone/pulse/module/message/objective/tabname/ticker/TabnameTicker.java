package net.flectone.pulse.module.message.objective.tabname.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class TabnameTicker extends AbstractTicker {

    @Inject
    public TabnameTicker(TabnameModule tabnameModule) {
        super(tabnameModule::add);
    }
}
