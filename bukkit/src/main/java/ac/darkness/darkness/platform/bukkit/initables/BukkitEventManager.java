package ac.darkness.darkness.platform.bukkit.initables;

import ac.darkness.darkness.manager.init.start.StartableInitable;
import ac.darkness.darkness.platform.bukkit.DarknessFreeBukkitLoaderPlugin;
import ac.darkness.darkness.platform.bukkit.events.PistonEvent;
import ac.darkness.darkness.utils.anticheat.LogUtil;
import org.bukkit.Bukkit;

public class BukkitEventManager implements StartableInitable {
    public void start() {
        LogUtil.info("Registering singular bukkit event... (PistonEvent)");

        Bukkit.getPluginManager().registerEvents(new PistonEvent(), DarknessFreeBukkitLoaderPlugin.LOADER);
    }
}
