package ac.darkness.darkness.platform.fabric.initables;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.manager.init.start.AbstractTickEndEvent;
import ac.darkness.darkness.platform.fabric.FabricServerEvents;
import ac.darkness.darkness.player.GrimPlayer;

public class FabricTickEndEvent extends AbstractTickEndEvent {

    @Override
    public void start() {
        if (!super.shouldInjectEndTick()) {
            return;
        }

        FabricServerEvents.onEndTick(server -> tickAllPlayers());
    }

    private void tickAllPlayers() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player.disableGrim) continue;
            super.onEndOfTick(player, true);
        }
    }
}
