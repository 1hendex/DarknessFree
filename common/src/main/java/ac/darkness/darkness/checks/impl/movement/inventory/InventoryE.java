package ac.darkness.darkness.checks.impl.movement.inventory;

import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.InventoryCheck;
import ac.darkness.darkness.checks.type.PacketSendListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "InventoryE", configName = "InventoryE", stableKey = "grim.inventory.held_item", setback = 3, description = "Sent a held item change packet while inventory is open")
public class InventoryE extends InventoryCheck implements PacketSendListener {
    private long lastTransaction = Long.MAX_VALUE; // Impossible transaction ID

    public InventoryE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        super.onPacketReceive(event);

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            // It is not possible to change hotbar slots with held item change while the inventory is open
            // A container click packet would be sent instead
            if (player.hasInventoryOpen) {
                if (this.lastTransaction < player.lastTransactionReceived.get()
                        && flagAndAlert()) {
                    // Cancel the packet
                    if (shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                        player.getInventory().needResend = true;
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

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            this.lastTransaction = player.lastTransactionSent.get();
        }
    }
}
