package ac.darkness.darkness.predictionengine.predictions;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.VectorData;
import ac.darkness.darkness.utils.math.Vector3dm;
import ac.darkness.darkness.utils.nmsutil.ReachUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PredictionEngineWaterLegacy extends PredictionEngine {

    private float swimmingFriction;

    public void guessBestMovement(float swimmingSpeed, GrimPlayer player, float swimmingFriction) {
        this.swimmingFriction = swimmingFriction;
        super.guessBestMovement(swimmingSpeed, player);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
        // Pre-1.13 firework elytra still boosts deltaMovement while gliding through water; model it before drag.
        if ((player.isGliding || player.wasGliding) && player.fireworks.getMaxFireworksAppliedPossible() > 0) {
            int maxRockets = PredictionEngineElytra.cappedFireworksForBoost(player);
            Vector3dm[] looks = {ReachUtils.getLook(player, player.yaw, player.pitch), ReachUtils.getLook(player, player.lastYaw, player.lastPitch)};
            Set<VectorData> expanded = new HashSet<>(possibleVectors);
            for (VectorData base : possibleVectors) {
                for (int n = 1; n <= maxRockets; n++) {
                    for (Vector3dm look : looks) {
                        Vector3dm boosted = PredictionEngineElytra.applyFireworkBoostPreservingJump(base.vector, look, n, base.isJump());
                        expanded.add(base.returnNewModified(boosted, base.vectorType));
                    }
                }
            }
            possibleVectors = expanded;
        }
        return super.applyInputsToVelocityPossibilities(player, possibleVectors, speed);
    }

    @Override
    public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
        for (VectorData vector : new HashSet<>(existingVelocities)) {
            existingVelocities.add(new VectorData(vector.vector.clone().add(0, 0.04f, 0), vector, VectorData.VectorType.Jump));

            if (player.skippedTickInActualMovement) {
                existingVelocities.add(new VectorData(vector.vector.clone().add(0, 0.02f, 0), vector, VectorData.VectorType.Jump));
            }
        }
    }

    @Override
    public void endOfTick(GrimPlayer player, double playerGravity) {
        super.endOfTick(player, playerGravity);

        for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
            vector.vector.multiply(swimmingFriction, 0.8F, swimmingFriction);

            // Gravity
            vector.vector.setY(vector.vector.getY() - 0.02D);
        }
    }
}
