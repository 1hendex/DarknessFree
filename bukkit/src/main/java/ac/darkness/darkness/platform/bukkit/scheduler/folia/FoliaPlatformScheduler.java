package ac.darkness.darkness.platform.bukkit.scheduler.folia;

import ac.darkness.darkness.platform.api.scheduler.PlatformScheduler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class FoliaPlatformScheduler implements PlatformScheduler {
    private final @NotNull FoliaAsyncScheduler asyncScheduler = new FoliaAsyncScheduler();
    private final @NotNull FoliaGlobalRegionScheduler globalRegionScheduler = new FoliaGlobalRegionScheduler();
    private final @NotNull FoliaEntityScheduler entityScheduler = new FoliaEntityScheduler();
    private final @NotNull FoliaRegionScheduler regionScheduler = new FoliaRegionScheduler();
}
