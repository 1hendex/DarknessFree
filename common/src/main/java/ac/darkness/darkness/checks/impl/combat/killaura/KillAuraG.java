package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;
import ac.darkness.darkness.utils.math.GrimMath;
import com.github.retrooper.packetevents.protocol.player.GameMode;

import java.util.Deque;
import java.util.LinkedList;

@CheckData(name = "KillAuraG", alternativeName = "KillAuraQ", configName = "KillAuraG", stableKey = "grim.killaura.invalid_sensitivity", description = "Checks for invalid sensitivity")
public class KillAuraG extends Check implements RotationListener {
    private final Deque<Float> delays = new LinkedList<>();

    public KillAuraG(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.gamemode == GameMode.SPECTATOR
                || player.inVehicle() || (time() - player.joinTime) < 5000L) {
            return;
        }

        float pitch = rotationUpdate.deltaPitchABS();
        float yaw = rotationUpdate.deltaYawABS();
        if ((yaw > 0.0f || pitch > 0.0f) && (time() - player.getLastHit()) < 1000L && player.deltaXZ > 0.1
                && player.actionManager.getVovka() > 0.6) {
            delays.add(pitch);
        }

        if (delays.size() == 40) {
            double avg = GrimMath.getAverage(delays);
            double std = GrimMath.getStandardDeviation(delays);
            double osc = GrimMath.getOscillation(delays);
            int sensitivity = (int) (player.getHorizontalSensitivity() * 200);

            if (osc > 40.0 && std > 12.5 && avg > 25.0) {
                if (++violations > (sensitivity > 90 ? 5 : 2)) {
                    flagAndAlert(osc + " | " + std + " | " + avg);
                }
            } else {
                violations = Math.max(violations - (sensitivity > 90 ? 0.2 : 0.05), -0.2);
            }
            delays.clear();
        }
    }
}
