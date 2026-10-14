package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.impl.combat.aim.processor.AimProcessor;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;
import ac.darkness.darkness.utils.lists.EvictingList;
import ac.darkness.darkness.utils.math.GrimMath;
import com.github.retrooper.packetevents.protocol.player.GameMode;

import java.util.Map;

@CheckData(name = "KillAuraE", alternativeName = "KillAuraJ", configName = "KillAuraE", stableKey = "grim.killaura.rotations_occurrence", description = "Checks for rotations value occurrence")
public class KillAuraE extends Check implements RotationListener {
    private final EvictingList<Float> samplesYaw = new EvictingList<>(50);
    private final EvictingList<Float> samplesPitch = new EvictingList<>(50);

    public KillAuraE(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.gamemode == GameMode.SPECTATOR
                || player.inVehicle() || (time() - player.joinTime) < 5000L || (time() - player.getLastHit()) > 500L) {
            return;
        }

        AimProcessor aimProcessor = player.checkManager.get(AimProcessor.class);
        final double deltaMouseY = aimProcessor.deltaDotsPitch;
        final double deltaMouseX = aimProcessor.deltaDotsYaw;

        float yaw = rotationUpdate.newYaw();
        if (Math.abs(yaw) != 0 && deltaMouseY >= 1 && deltaMouseX >= 1) {
            samplesYaw.add(yaw);
        }
        if (samplesYaw.isFull()) {
            Map.Entry<Float, Long> highYaw = GrimMath.getHigh(samplesYaw);
            if (highYaw != null) {
                final float valueYaw = highYaw.getKey();
                final long paternYaw = highYaw.getValue();
                flagIf(valueYaw != 0 && paternYaw > 25, "(Yaw) " + yaw);
            }
        }

        float pitch = rotationUpdate.newPitch();
        if (Math.abs(pitch) != 90 && deltaMouseX >= 5 && deltaMouseY >= 5) {
            samplesPitch.add(pitch);
        }
        if (samplesPitch.isFull()) {
            Map.Entry<Float, Long> highPitch = GrimMath.getHigh(samplesPitch);
            if (highPitch != null) {
                final float valuePitch = highPitch.getKey();
                final long paternPitch = highPitch.getValue();
                flagIf(valuePitch != 0 && paternPitch > 25, "(Pitch) " + formatOffset(pitch));
            }
        }
    }
}
