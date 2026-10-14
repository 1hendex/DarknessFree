package ac.darkness.darkness.checks.impl.movement;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.PositionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PositionUpdate;

public class PredictionRunner extends GrimProcessor implements PositionListener {

    public PredictionRunner(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPositionUpdate(final PositionUpdate positionUpdate) {
        if (!player.inVehicle()) {
            player.movementCheckRunner.processAndCheckMovementPacket(positionUpdate);
        }
    }
}
