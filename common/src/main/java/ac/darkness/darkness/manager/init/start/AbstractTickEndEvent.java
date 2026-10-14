package ac.darkness.darkness.manager.init.start;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.player.GrimPlayer;

// Intended for future events we inject all platforms at the end of a tick
public abstract class AbstractTickEndEvent implements StartableInitable {

    @Override
    public void start() {

    }

    protected void onEndOfTick(GrimPlayer player, boolean flush) {
        player.packetEntityReplication.onEndOfTickEvent(true, flush);
    }

    protected boolean shouldInjectEndTick() {
        return GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("Reach.enable-post-packet", false);
    }
}
