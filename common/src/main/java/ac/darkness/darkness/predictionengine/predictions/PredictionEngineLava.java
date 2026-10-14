package ac.darkness.darkness.predictionengine.predictions;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.VectorData;
import ac.darkness.darkness.utils.enums.FluidTag;
import ac.darkness.darkness.utils.math.GrimMath;
import ac.darkness.darkness.utils.math.Vector3dm;
import ac.darkness.darkness.utils.nmsutil.ReachUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PredictionEngineLava extends PredictionEngine {
    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
        // Lava keeps the glide flag, so an active firework still boosts deltaMovement before lava physics.
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
            if (player.couldSkipTick && vector.isZeroPointZeroThree()) {
                double extraVelFromVertTickSkipUpwards = GrimMath.clamp(player.actualMovement.getY(), vector.vector.clone().getY(), vector.vector.clone().getY() + 0.05f);
                existingVelocities.add(new VectorData(vector.vector.clone().setY(extraVelFromVertTickSkipUpwards), vector, VectorData.VectorType.Jump));
            } else {
                existingVelocities.add(new VectorData(vector.vector.clone().add(0, 0.04f, 0), vector, VectorData.VectorType.Jump));
            }

            if (player.getFluidHeight(FluidTag.LAVA) <= 0.4D && player.lastOnGround && !player.onGround) {
                Vector3dm withJump = vector.vector.clone();
                super.doJump(player, withJump);
                existingVelocities.add(new VectorData(withJump, vector, VectorData.VectorType.Jump));
            }
        }
    }
}
