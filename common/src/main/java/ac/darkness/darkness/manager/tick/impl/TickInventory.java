package ac.darkness.darkness.manager.tick.impl;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.manager.tick.Tickable;
import ac.darkness.darkness.player.GrimPlayer;

public class TickInventory implements Tickable {
    @Override
    public void tick() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.inventory.inventory.getInventoryStorage().tickWithBukkit();
        }
    }
}
