package ac.darkness.darkness.checks.impl.movement.inventory;

import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.BlockPlaceListener;
import ac.darkness.darkness.checks.type.InventoryCheck;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockPlace;

@CheckData(name = "InventoryC", configName = "InventoryC", stableKey = "grim.inventory.block_place", setback = 3, description = "Placed a block while inventory is open")
public class InventoryC extends InventoryCheck implements BlockPlaceListener {

    public InventoryC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        // It is not possible to place a block while the inventory is open
        if (player.hasInventoryOpen) {
            if (flagAndAlert()) {
                if (shouldModifyPackets()) {
                    place.resync();
                }
                if (!isNoSetbackPermission()) {
                    closeInventory();
                }
            }
        } else {
            reward();
        }
    }
}
