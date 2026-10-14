package ac.darkness.darkness.checks.impl.movement.inventory;

import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.InventoryCheck;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "InventoryB", configName = "InventoryB", stableKey = "grim.inventory.dig", setback = 3, description = "Started digging blocks while inventory is open")
public class InventoryB extends InventoryCheck {
    public InventoryB(GrimPlayer player) {
        super(player);
    }

    public void handle(PacketReceiveEvent event, WrapperPlayClientPlayerDigging wrapper) {
        if (wrapper.getAction() != DiggingAction.START_DIGGING) return;

        // Is not possible to start digging a block while the inventory is open.
        if (player.hasInventoryOpen) {
            if (flagAndAlert()) {
                // Cancel the packet
                if (shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
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
