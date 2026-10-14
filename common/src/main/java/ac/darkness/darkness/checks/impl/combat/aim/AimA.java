package ac.darkness.darkness.checks.impl.combat.aim;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimA", alternativeName = "AimDuplicateLook", configName = "AimA", stableKey = "grim.aim.duplicate_look", description = "Sent a duplicate rotation update without changing look direction")
public class AimA extends Check implements RotationListener {
    private boolean exempt;

    public AimA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.packetStateData.lastPacketWasOnePointSeventeenDuplicate || player.compensatedEntities.self.getRiding() != null) {
            exempt = true;
            return;
        }

        if (exempt) { // Exempt for a tick on teleport
            exempt = false;
            return;
        }

        if (rotationUpdate.oldYaw() == rotationUpdate.newYaw() && rotationUpdate.oldPitch() == rotationUpdate.newPitch()) {
            flag();
        }
    }
}
