package ac.darkness.darkness.manager.tick.impl;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.manager.config.BaseConfigManager;
import ac.darkness.darkness.manager.tick.Tickable;
import ac.darkness.darkness.player.GrimPlayer;

public class TickPermissions implements Tickable {

    @Override
    public void tick() {
        BaseConfigManager config = GrimAPI.INSTANCE.getConfigManager();
        int interval = config.getUpdatePermissionTicks();
        if (interval <= 0 || GrimAPI.INSTANCE.getTickManager().currentTick % interval != 0) return;

        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.updatePermissions();
        }
    }
}
