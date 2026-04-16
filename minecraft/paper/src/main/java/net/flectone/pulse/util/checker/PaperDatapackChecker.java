package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.bukkit.Bukkit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperDatapackChecker {

    // if datapack file has been renamed, it cannot be checked here,
    // API does not provide the ability to check this anyway
    public boolean isEnabled(String name) {
        return Bukkit.getDatapackManager().getEnabledPacks().stream()
                .anyMatch(datapack -> Strings.CI.contains(datapack.getName(), name));
    }

}
