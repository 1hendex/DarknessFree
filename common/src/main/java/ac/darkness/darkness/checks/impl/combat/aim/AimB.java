package ac.darkness.darkness.checks.impl.combat.aim;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;

// Based on Kauri AimA,
// I also discovered this flaw before open source Kauri, but did not want to open source its detection.
// It works on clients who % 360 their rotation.
@CheckData(name = "AimB", alternativeName = "AimModulo360", configName = "AimB", stableKey = "grim.aim.modulo_360", description = "Sent a large yaw snap", decay = 0.005)
public class AimB extends Check implements RotationListener {

    private float lastDeltaYaw;

    public AimB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        // Exempt for teleport, entering a vehicle due to rotation reset or
        // after forced, client-sided rotation change after interacting with a horse (not necessarily mounting it)
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch
                || player.packetStateData.horseInteractCausedForcedRotation) {
            lastDeltaYaw = rotationUpdate.deltaYaw();
            return;
        }

        if (player.yaw < 360 && player.yaw > -360 && Math.abs(rotationUpdate.deltaYaw()) > 320 && Math.abs(lastDeltaYaw) < 30) {
            flag();
        } else {
            reward();
        }

        lastDeltaYaw = rotationUpdate.deltaYaw();
    }
}
