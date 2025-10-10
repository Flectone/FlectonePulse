package net.flectone.pulse.platform.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.checker.PermissionChecker;

import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RangeFilter {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    public Predicate<FPlayer> createFilter(FPlayer filterPlayer, Range range) {
        if (range.is(Range.Type.PLAYER)) {
            return filterPlayer::equals;
        }

        if (!(filterPlayer instanceof FPlayer fPlayer) || fPlayer.isUnknown()) {
            return player -> true;
        }

        return fReceiver -> {
            if (fReceiver.isUnknown()) return true;
            if (fReceiver.isIgnored(fPlayer)) return false;

            return switch (range.getType()) {
                case BLOCKS -> checkDistance(fPlayer, fReceiver, range.getValue());
                case WORLD_NAME -> checkWorldNamePermission(fPlayer, fReceiver);
                case WORLD_TYPE -> checkWorldTypePermission(fPlayer, fReceiver);
                default -> true;
            };
        };
    }

    public boolean checkDistance(FPlayer fPlayer, FPlayer fReceiver, int range) {
        double distance = platformPlayerAdapter.distance(fPlayer, fReceiver);
        return distance != -1.0 && distance <= range;
    }

    public boolean checkWorldNamePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldName = platformPlayerAdapter.getWorldName(fPlayer);
        if (worldName.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "flectonepulse.world.name." + worldName);
    }

    public boolean checkWorldTypePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldType = platformPlayerAdapter.getWorldEnvironment(fPlayer);
        if (worldType.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "flectonepulse.world.type." + worldType);
    }

}
