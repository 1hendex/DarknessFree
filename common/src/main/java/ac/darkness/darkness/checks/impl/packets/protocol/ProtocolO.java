package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

@CheckData(name = "ProtocolO", configName = "ProtocolO", stableKey = "grim.protocol.hand_usage", description = "Checks for invalid hand usage")
public class ProtocolO extends Check implements PacketReceiveListener {

    public ProtocolO(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) return;
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            WrapperPlayClientUseItem use = new WrapperPlayClientUseItem(event);
            if (player.packetStateData.isSlowedByUsingItem() &&
                    (player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot()
                            || player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND)) {
                if (use.getHand() != player.packetStateData.itemInUseHand) {
                    if (flag("hand=" + player.packetStateData.itemInUseHand.name() +
                            ", against=" + use.getHand().name()) && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                }
            }
        }
    }
}
