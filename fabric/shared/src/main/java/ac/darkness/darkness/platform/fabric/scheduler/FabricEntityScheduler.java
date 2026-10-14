package ac.darkness.darkness.platform.fabric.scheduler;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.darkness.darkness.platform.api.entity.GrimEntity;
import ac.darkness.darkness.platform.api.scheduler.EntityScheduler;
import ac.darkness.darkness.platform.api.scheduler.TaskHandle;
import ac.darkness.darkness.platform.fabric.AbstractGrimACFabricEntryPoint;
import ac.darkness.darkness.platform.fabric.FabricServerEvents;
import ac.darkness.darkness.platform.fabric.inject.FabricMinecraftServerHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FabricEntityScheduler implements EntityScheduler {
    private final Map<FabricPlatformScheduler.ScheduledTask, Runnable> taskMap = new ConcurrentHashMap<>();

    public FabricEntityScheduler() {
        FabricServerEvents.onEndTick(this::handleTasks);
    }

    private void handleTasks(FabricMinecraftServerHandle server) {
        FabricPlatformScheduler.handleSyncTasks(taskMap, server);
    }

    @Override
    public void execute(@NotNull GrimEntity entity, @NotNull GrimPlugin plugin, @NotNull Runnable run, @Nullable Runnable retired, long delay) {
        runDelayed(entity, plugin, run, retired, delay);
    }

    @Override
    public TaskHandle run(@NotNull GrimEntity entity, @NotNull GrimPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired) {
        return runDelayed(entity, plugin, task, retired, 0);
    }

    @Override
    public TaskHandle runDelayed(@NotNull GrimEntity entity, @NotNull GrimPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long delayTicks) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                () -> {
                    task.run();
                    if (retired != null && entity.isDead()) {
                        retired.run();
                    }
                },
                AbstractGrimACFabricEntryPoint.server().getTickCount() + delayTicks,
                0,
                false,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true);
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull GrimEntity entity, @NotNull GrimPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                () -> {
                    task.run();
                    if (retired != null && entity.isDead()) {
                        retired.run();
                    }
                },
                AbstractGrimACFabricEntryPoint.server().getTickCount() + initialDelayTicks,
                periodTicks,
                true,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true);
    }

    public void cancel(@NotNull GrimPlugin plugin) {
        FabricPlatformScheduler.cancelPluginTasks(taskMap, plugin);
    }

    public void cancelAll() {
        FabricPlatformScheduler.cancelAllTasks(taskMap);
    }
}
