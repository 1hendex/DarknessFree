package ac.darkness.darkness.platform.bukkit.initables;

import ac.darkness.darkness.manager.init.start.StartableInitable;
import ac.darkness.darkness.platform.bukkit.DarknessFreeBukkitLoaderPlugin;
import ac.darkness.darkness.utils.anticheat.Constants;
import io.github.retrooper.packetevents.bstats.bukkit.Metrics;

public class BukkitBStats implements StartableInitable {
    @Override
    public void start() {
        try {
            new Metrics(DarknessFreeBukkitLoaderPlugin.LOADER, Constants.BSTATS_PLUGIN_ID);
        } catch (Exception ignored) {}
    }
}
