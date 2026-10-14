package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.impl.combat.aim.processor.RotationProcessor;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "KillAuraH", alternativeName = "KillAuraR", configName = "KillAuraH", stableKey = "grim.killaura.acceleration", description = "Checks for invalid yaw and pitch acceleration", experimental = true)
public class KillAuraH extends Check implements RotationListener {

    public KillAuraH(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.gamemode == GameMode.SPECTATOR
                || player.inVehicle() || (time() - player.joinTime) < 5000L) {
            return;
        }

        if ((time() - player.getLastHit()) < 5000L) {
            RotationProcessor processor = player.rotationProcessor;
            final float pitchAccel = processor.getAccelPitch();
            final float yawAccel = processor.getAccelYaw();
            final float deltaYaw = processor.getDeltaYaw();
            final float deltaPitch = processor.getDeltaPitch();

            boolean invalidA = (yawAccel > 0 && yawAccel < 1E-3 && pitchAccel < 1E-4);
            boolean invalidB = (pitchAccel == 0.0f && yawAccel == 0.0f);
            if (deltaYaw > 0.05f || deltaPitch > 0.05f) {
                if (invalidA || invalidB) {
                    if (violations++ > (invalidB && deltaYaw > 60.0f ? 5 : 20)) {
                        flagAndAlert("Type: A | " + deltaYaw + " | " + yawAccel + " | " + pitchAccel);
                    }
                } else {
                    violations = Math.max(0, violations - 2.0);
                }
            }
        }
    }
}
