package ac.darkness.darkness.checks.impl.combat.aim.processor;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.RotationListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.RotationUpdate;
import lombok.Getter;

@Getter
public class RotationProcessor extends GrimProcessor implements RotationListener {
    private float yaw, pitch, lastYaw, lastPitch, deltaYaw, deltaYawABS, deltaPitchABS, deltaPitch;
    private float lastDeltaYawABS, lastDeltaPitchABS, accelYaw, accelPitch;

    public RotationProcessor(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport) {
            return;
        }

        yaw = rotationUpdate.newYaw();
        pitch = rotationUpdate.newPitch();

        lastDeltaPitchABS = deltaPitchABS;
        lastDeltaYawABS = deltaYawABS;

        deltaYaw = rotationUpdate.deltaYaw();
        deltaPitch = rotationUpdate.deltaPitch();
        deltaYawABS = rotationUpdate.deltaYawABS();
        deltaPitchABS = rotationUpdate.deltaPitchABS();

        accelYaw = deltaYawABS - lastDeltaYawABS;
        accelPitch = deltaPitchABS - lastDeltaPitchABS;

        lastYaw = rotationUpdate.oldYaw();
        lastPitch = rotationUpdate.oldPitch();
    }
}
