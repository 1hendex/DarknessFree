package ac.darkness.darkness.checks.impl.misc.crash;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PrePredictionPacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;

@CheckData(name = "CrashA", stableKey = "grim.crash.large_position", description = "Sent a position outside the valid world bounds")
public class CrashA extends Check implements PrePredictionPacketReceiveListener {
    private static final double HARD_CODED_BORDER = 2.9999999E7D;

    public CrashA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPrePredictionPacketReceive(PacketReceiveEvent event) {
        if (player.packetStateData.lastPacketWasTeleport) return;
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

            if (!packet.hasPositionChanged()) return;
            // Y technically is uncapped, but no player will reach these values legit
            if (Math.abs(packet.getLocation().getX()) > HARD_CODED_BORDER || Math.abs(packet.getLocation().getZ()) > HARD_CODED_BORDER || Math.abs(packet.getLocation().getY()) > Integer.MAX_VALUE) {
                flag(); // Ban
                executeViolationSetback();
                event.setCancelled(true);
                player.onPacketCancel();
            }
        } else if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            WrapperPlayClientVehicleMove packet = new WrapperPlayClientVehicleMove(event);
            Vector3d pos = packet.getPosition();
            if (Math.abs(pos.getX()) > HARD_CODED_BORDER || Math.abs(pos.getZ()) > HARD_CODED_BORDER || Math.abs(pos.getY()) > Integer.MAX_VALUE) {
                flag(); // Ban
                executeViolationSetback();
                event.setCancelled(true);
                player.onPacketCancel();
            }
        }
    }
}
