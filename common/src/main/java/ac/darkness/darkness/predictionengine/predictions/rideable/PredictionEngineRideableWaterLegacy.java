package ac.darkness.darkness.predictionengine.predictions.rideable;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.predictionengine.predictions.PredictionEngineWaterLegacy;
import ac.darkness.darkness.predictionengine.predictions.input.Input;
import ac.darkness.darkness.utils.data.VectorData;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PredictionEngineRideableWaterLegacy extends PredictionEngineWaterLegacy {
    private final Input movementVector;

    @Override
    public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
        PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(movementVector, player, possibleVectors, speed);
    }
}
