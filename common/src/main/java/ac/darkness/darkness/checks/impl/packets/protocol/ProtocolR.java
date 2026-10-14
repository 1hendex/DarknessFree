package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

@CheckData(name = "ProtocolR", alternativeName = "PacketOrderQ", configName = "ProtocolR", stableKey = "grim.packetorder.position_no_transaction", description = "Checks for position changed without transactions packet", experimental = true)
public class ProtocolR extends Check implements PacketReceiveListener {
    private int positions = 0;
    private long lastTransTime = 0L;

    public ProtocolR(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon type = event.getPacketType();
        long now = event.getTimestamp();

        // If a valid transaction has arrived, we reset the counter.
        if (isTransaction(type) && player.packetStateData.lastTransactionPacketWasValid) {
            positions = 0;
            lastTransTime = now;
            return;
        }

        // Count the pos packets
        if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || type == PacketType.Play.Client.PLAYER_POSITION) {
            positions++;

            // If 40 position packets have already arrived (~2 seconds), but there are zero transactions
            if (positions > 47 && now - player.joinTime > 5000L) {
                flag("positions=" + positions + ", last=" + (now - lastTransTime) + "ms");
                player.getSetbackTeleportUtil().executeViolationSetback();

                positions = 0;
            }
        }
    }
}
