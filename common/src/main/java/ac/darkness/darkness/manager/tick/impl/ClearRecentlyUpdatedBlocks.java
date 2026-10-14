package ac.darkness.darkness.manager.tick.impl;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.manager.tick.Tickable;
import ac.darkness.darkness.player.GrimPlayer;

public class ClearRecentlyUpdatedBlocks implements Tickable {

    private static final int maxTickAge = 2;

    @Override
    public void tick() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.blockHistory.cleanup(GrimAPI.INSTANCE.getTickManager().currentTick - maxTickAge);
        }
    }
}
