package ac.darkness.darkness.checks.impl.movement;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.VehicleListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PositionUpdate;
import ac.darkness.darkness.utils.anticheat.update.VehiclePositionUpdate;

public class VehiclePredictionRunner extends GrimProcessor implements VehicleListener {
    public VehiclePredictionRunner(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final VehiclePositionUpdate vehicleUpdate) {
        // Vehicle onGround = false always
        // We don't do vehicle setbacks because vehicle netcode sucks.
        player.movementCheckRunner.processAndCheckMovementPacket(new PositionUpdate(vehicleUpdate.from(), vehicleUpdate.to(), false, null, null, vehicleUpdate.isTeleport()));
    }
}
