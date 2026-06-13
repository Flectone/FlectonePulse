package net.flectone.pulse.platform.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;

import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RangeFilter {

    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    public Predicate<FPlayer> createFilter(EventMetadata<?> eventMetadata) {
        Predicate<FPlayer> filter = eventMetadata.filter();

        boolean hasCustomFilter = !filter.test(FPlayer.UNKNOWN);
        if (eventMetadata.range().is(Range.Type.PLAYER) && hasCustomFilter) {
            return filter;
        }

        return filter.and(createFilter(eventMetadata.sender(), eventMetadata.range()));
    }

    public Predicate<FPlayer> createFilter(FEntity filterPlayer, Range range) {
        if (range.is(Range.Type.PLAYER)) {
            return filterPlayer::equals;
        }

        if (!(filterPlayer instanceof FPlayer fPlayer) || fPlayer.isUnknown()) {
            return _ -> true;
        }

        return fReceiver -> {
            if (fReceiver.isUnknown() || fReceiver.isConsole()) return true;
            if (socialService.isIgnored(fReceiver, fPlayer)) return false;

            return switch (range.type()) {
                case BLOCKS -> checkDistance(fPlayer, fReceiver, range.value());
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
