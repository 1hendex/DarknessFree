package ac.darkness.darkness.checks.impl.movement.elytra;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

@CheckData(name = "ElytraK", alternativeName = "ElytraC", configName = "ElytraK", stableKey = "grim.elytra.invalid_vertical_boost", description = "Checks for invalid elytra vertical boost", setback = 1)
public class ElytraK extends Check implements PacketReceiveListener {
    private long lastFireworkTime = 0;

    public ElytraK(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)
                || !player.isGliding || player.packetStateData.lastPacketWasTeleport
                || player.glideTicks < 10 || player.isSwimming || player.isInWeb || player.isInBerryBush) {
            return;
        }
        boolean firework = player.fireworks.getMaxFireworksAppliedPossible() > 0
                || player.fireworks.hasFirework(player.entityID);

        if (firework) lastFireworkTime = time();
        if (time() - lastFireworkTime < 3200) return;
        final double accelXZ = player.accelXZ;
        final double deltaY = player.deltaY;
        final double deltaXZ = player.deltaXZ;
        final float pitch = player.pitch;
        boolean invalid = deltaY > 0.01 && Math.abs(pitch) == 0.0f && accelXZ < 1E-4 && deltaXZ < 0.05;

        boolean air = player.compensatedWorld.getBlock(player.x, player.y, player.z).getType() == StateTypes.AIR;
        if (invalid && air) {
            violations += (deltaXZ < 0.01 ? 1.5 : 1.0);
            if (++violations > 5) {
                flagAndAlert("Type: A | " + Math.floor(Math.sqrt(deltaY) * 100) / 100 +
                        " | " + Math.floor(Math.sqrt(accelXZ) * 100) / 100);
            }
        } else {
            violations = Math.max(0, violations - 1);
        }
    }
}
