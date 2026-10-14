package ac.darkness.darkness.platform.fabric.initables;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.manager.init.start.StartableInitable;
import ac.darkness.darkness.manager.init.stop.StoppableInitable;
import ac.darkness.darkness.platform.fabric.utils.metrics.MetricsFabric;
import ac.darkness.darkness.utils.anticheat.Constants;

public class FabricBStats implements StartableInitable, StoppableInitable {

    private MetricsFabric metricsFabric;

    @Override
    public void start() {
        try {
            metricsFabric = new MetricsFabric(GrimAPI.INSTANCE.getGrimPlugin(), Constants.BSTATS_PLUGIN_ID);
        } catch (Exception ignored) {}
    }

    @Override
    public void stop() {
        if (metricsFabric != null)
            metricsFabric.shutdown();
    }
}
