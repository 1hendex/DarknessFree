package ac.darkness.darkness.platform.bukkit.scheduler.folia;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.darkness.darkness.platform.api.scheduler.RegionScheduler;
import ac.darkness.darkness.platform.api.scheduler.TaskHandle;
import ac.darkness.darkness.platform.api.world.PlatformWorld;
import ac.darkness.darkness.platform.bukkit.DarknessFreeBukkitLoaderPlugin;
import ac.darkness.darkness.platform.bukkit.world.BukkitPlatformWorld;
import ac.darkness.darkness.utils.math.Location;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FoliaRegionScheduler implements RegionScheduler {

    private final io.papermc.paper.threadedregions.scheduler.RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

    @Override
    public void execute(@NotNull GrimPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        regionScheduler.execute(DarknessFreeBukkitLoaderPlugin.LOADER, ((BukkitPlatformWorld) world).bukkitWorld(), chunkX, chunkZ, task);
    }

    @Override
    public void execute(@NotNull GrimPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        execute(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
    }

    @Override
    public TaskHandle run(@NotNull GrimPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        return new FoliaTaskHandle(regionScheduler.run(
                DarknessFreeBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).bukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run()
        ));
    }

    @Override
    public TaskHandle run(@NotNull GrimPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        return run(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
    }

    @Override
    public TaskHandle runDelayed(@NotNull GrimPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long delayTicks) {
        return new FoliaTaskHandle(regionScheduler.runDelayed(
                DarknessFreeBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).bukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run(),
                delayTicks
        ));
    }

    @Override
    public TaskHandle runDelayed(@NotNull GrimPlugin plugin, @NotNull Location location, @NotNull Runnable task, long delayTicks) {
        return runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, delayTicks);
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull GrimPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new FoliaTaskHandle(regionScheduler.runAtFixedRate(
                DarknessFreeBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).bukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run(),
                initialDelayTicks,
                periodTicks
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull GrimPlugin plugin, @NotNull Location location, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return runAtFixedRate(
                plugin,
                location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                initialDelayTicks,
                periodTicks
        );
    }
}
