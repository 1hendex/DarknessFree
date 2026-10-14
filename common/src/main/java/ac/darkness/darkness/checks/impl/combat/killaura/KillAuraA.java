package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "KillAuraA", stableKey = "grim.killaura.zero_pitch", description = "Checks for zero pitch rotations")
public class KillAuraA extends Check implements RotationListener {
    public KillAuraA(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.gamemode == GameMode.SPECTATOR
                || player.inVehicle() || (time() - player.joinTime) < 5000 || (time() - player.getLastHit()) > 500) {
            return;
        }
        final float pitch = rotationUpdate.newPitch();

        flagIf(pitch == 0, "");
    }
}
