package ac.darkness.darkness.predictionengine.movementtick;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.predictionengine.predictions.PredictionEngineLava;
import ac.darkness.darkness.predictionengine.predictions.PredictionEngineNormal;
import ac.darkness.darkness.predictionengine.predictions.PredictionEngineWater;
import ac.darkness.darkness.predictionengine.predictions.PredictionEngineWaterLegacy;
import ac.darkness.darkness.utils.nmsutil.BlockProperties;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public class MovementTickerPlayer extends MovementTicker {
    public MovementTickerPlayer(GrimPlayer player) {
        super(player);
    }

    @Override
    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            new PredictionEngineWater().guessBestMovement(swimSpeed, player, isFalling, player.gravity, swimFriction);
        } else {
            new PredictionEngineWaterLegacy().guessBestMovement(swimSpeed, player, swimFriction);
        }
    }

    @Override
    public void doLavaMove() {
        new PredictionEngineLava().guessBestMovement(0.02F, player);
    }

    @Override
    public void doNormalMove(float blockFriction) {
        new PredictionEngineNormal().guessBestMovement(BlockProperties.getFrictionInfluencedSpeed(blockFriction, player), player);
    }
}
