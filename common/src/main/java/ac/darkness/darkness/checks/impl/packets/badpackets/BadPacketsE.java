package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsE", alternativeName = "BadPacketsAA", configName = "BadPacketsE", stableKey = "grim.badpackets.micro_position", description = "Checks for impossible micro position")
public class BadPacketsE extends Check implements PacketReceiveListener {
    private int noReminderTicks;

    public BadPacketsE(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isTickPacket(event.getPacketType())) {
            return;
        }
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            if (!(player.horizontalCollision && !player.verticalCollision) && flying.hasPositionChanged()) {
                if (noReminderTicks++ >= 3) {
                    double distanceSq = flying.getLocation().getPosition()
                            .distanceSquared(new Vector3d(player.lastX, player.lastY, player.lastZ));
                    if (distanceSq <= 1.0E-12 && distanceSq != 0.0 && !player.inVehicle()) {
                        flagIf(++violations >= 2.5, "delta=" + distanceSq);
                    } else {
                        violations = Math.max(0, violations - 0.05);
                    }
                }
            } else {
                noReminderTicks = 0;
            }
        }
    }

    public void handleRespawn() {
        noReminderTicks = 0;
    }
}
