package ac.darkness.darkness.checks.impl.misc.crash;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PrePredictionPacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.predictionengine.MovementCheckRunner;
import ac.darkness.darkness.utils.math.VectorUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;

// The 1.17-1.20 Mojang duplicate position packet is isTickPacket=false, so it is exempt from the Timer
// throttle and never reaches the packet-spam counter. It also skips the prediction (handleFlying ignores
// its position), so a flood of them is cheap-but-unbounded. A legit client emits at most one per real
// tick; cap them at vanilla's own per-tick move ceiling (5) and let the surplus feed the spam kick.
// Copied from: https://github.com/KaelusAI/GroundedGrim/blob/2.0/common/src/main/java/ac/grim/grimac/checks/impl/crash/CrashJ.java
@CheckData(name = "CrashJ", stableKey = "grim.crash.duplicate_packet", description = "Checks for duplicate movement packet and non-physical movement exploits")
public class CrashJ extends Check implements PrePredictionPacketReceiveListener {
    private static final int MAX_DUPLICATES_BETWEEN_TICKS = 5;
    private int duplicates = 0; // netty-thread only
    private Vector3d lastWire = null;

    public CrashJ(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPrePredictionPacketReceive(PacketReceiveEvent event) {
        final PacketTypeCommon type = event.getPacketType();
        if (WrapperPlayClientPlayerFlying.isFlying(type)) {
            if (player.packetStateData.lastPacketWasOnePointSeventeenDuplicate) {
                // No setCancelled: the duplicate has no authoritative position to drop, so counting it
                // toward the spam kick cannot desync a legit client.
                if (++duplicates > MAX_DUPLICATES_BETWEEN_TICKS) player.onPacketCancel();
            } else if (!player.packetStateData.lastPacketWasTeleport) {
                duplicates = 0; // a real movement packet marks a new tick boundary
            }
        } else if (type == PacketType.Play.Client.VEHICLE_MOVE
                || type == PacketType.Play.Client.CLIENT_TICK_END) {
            duplicates = 0;
        }

        final boolean flying = WrapperPlayClientPlayerFlying.isFlying(type);
        final boolean vehicle = type == PacketType.Play.Client.VEHICLE_MOVE;
        if (!flying && !vehicle) return;

        final double rawX, rawY, rawZ;
        if (flying) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            if (!packet.hasPositionChanged()) return;
            rawX = packet.getLocation().getX();
            rawY = packet.getLocation().getY();
            rawZ = packet.getLocation().getZ();
        } else {
            Vector3d pos = new WrapperPlayClientVehicleMove(event).getPosition();
            rawX = pos.getX();
            rawY = pos.getY();
            rawZ = pos.getZ();
        }

        if (!Double.isFinite(rawX) || !Double.isFinite(rawY) || !Double.isFinite(rawZ)) return;

        final Vector3d clamped = VectorUtils.clampVector(new Vector3d(rawX, rawY, rawZ));
        if (player.packetStateData.lastPacketWasTeleport) {
            lastWire = clamped;
            return;
        }

        if (lastWire == null) {
            lastWire = new Vector3d(player.x, player.y, player.z);
        }

        final double dx = clamped.getX() - lastWire.getX();
        final double dy = clamped.getY() - lastWire.getY();
        final double dz = clamped.getZ() - lastWire.getZ();

        if (dx * dx + dy * dy + dz * dz >= MovementCheckRunner.NON_PHYSICAL_TICK_DISTANCE_SQUARED) {
            if (flying && !player.inVehicle()) {
                if (!player.getSetbackTeleportUtil().shouldBlockMovement()) {
                    flag();
                }
                executeViolationSetback();
            }
            event.setCancelled(true);
            player.onPacketCancel();
            return;
        }
        lastWire = clamped;
    }
}
