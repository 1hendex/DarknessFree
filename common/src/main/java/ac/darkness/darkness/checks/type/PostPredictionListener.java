package ac.darkness.darkness.checks.type;

import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;

public interface PostPredictionListener {
    void onPredictionComplete(PredictionComplete predictionComplete);
}
