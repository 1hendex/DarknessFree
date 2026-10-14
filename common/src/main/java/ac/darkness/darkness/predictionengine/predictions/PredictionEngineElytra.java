package ac.darkness.darkness.predictionengine.predictions;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.IndexedVector3d;
import ac.darkness.darkness.utils.data.VectorData;
import ac.darkness.darkness.utils.math.GrimMath;
import ac.darkness.darkness.utils.math.Vector3dm;
import ac.darkness.darkness.utils.nmsutil.ReachUtils;
import ac.darkness.darkness.utils.nmsutil.StuckSpeed;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PredictionEngineElytra extends PredictionEngine {
    public static Vector3dm getElytraMovement(GrimPlayer player, Vector3dm vector, Vector3dm lookVector) {
        float pitchRadians = GrimMath.radians(player.pitch);
        double horizontalSqrt = Math.sqrt(lookVector.getX() * lookVector.getX() + lookVector.getZ() * lookVector.getZ());
        double horizontalLength = vector.clone().setY(0).length();
        double length = lookVector.length();

        // Mojang changed from using their math to using regular java math in 1.18.2 elytra movement
        double vertCosRotation = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2) ? Math.cos(pitchRadians) : player.trigHandler.cos(pitchRadians);
        vertCosRotation = vertCosRotation * vertCosRotation * Math.min(1.0D, length / 0.4D);
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
            vertCosRotation = (float) vertCosRotation;
        }

        // So we actually use the player's actual movement to get the gravity/slow falling status
        // However, this is wrong with elytra movement because players can control vertical movement after gravity is calculated
        // Yeah, slow falling needs a refactor in grim.
        double recalculatedGravity = player.compensatedEntities.self.getAttributeValue(Attributes.GRAVITY);
        if (player.clientVelocity.getY() <= 0 && player.compensatedEntities.getSlowFallingAmplifier().isPresent()) {
            recalculatedGravity = player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5) ? 0.01 : Math.min(recalculatedGravity, 0.01);
        }

        vector.add(0.0D, recalculatedGravity * (-1.0D + vertCosRotation * 0.75D), 0.0D);
        double d5;

        // Handle slowing the player down when falling
        if (vector.getY() < 0.0D && horizontalSqrt > 0.0D) {
            d5 = vector.getY() * -0.1D * vertCosRotation;
            vector.add(lookVector.getX() * d5 / horizontalSqrt, d5, lookVector.getZ() * d5 / horizontalSqrt);
        }

        // Handle accelerating the player when they are looking down
        if (pitchRadians < 0.0F && horizontalSqrt > 0.0D) {
            d5 = horizontalLength * (double) (-player.trigHandler.sin(pitchRadians)) * 0.04D;
            vector.add(-lookVector.getX() * d5 / horizontalSqrt, d5 * 3.2D, -lookVector.getZ() * d5 / horizontalSqrt);
        }

        // Handle accelerating the player sideways
        if (horizontalSqrt > 0) {
            vector.add((lookVector.getX() / horizontalSqrt * horizontalLength - vector.getX()) * 0.1D, 0.0D, (lookVector.getZ() / horizontalSqrt * horizontalLength - vector.getZ()) * 0.1D);
        }

        return vector;
    }

    public static void applyFireworkBoost(Vector3dm velocity, Vector3dm look) {
        velocity.setX(velocity.getX() * 0.5 + look.getX() * 0.85);
        velocity.setY(velocity.getY() * 0.5 + look.getY() * 0.85);
        velocity.setZ(velocity.getZ() * 0.5 + look.getZ() * 0.85);
    }

    /**
     * Vanilla applies the firework boost to the rocket's own velocity, while the player's jump
     * input is layered on top of the (already boosted) velocity during travel — the jump is never
     * diluted by the boost. The fluid engines bake the +0.04 fluid jump into the base vector before
     * this expansion runs, so a plain boost would shrink it by 0.5^boosts. Re-add the diluted
     * portion so the predicted jump velocity matches vanilla (firework + water/lava jump).
     */
    public static Vector3dm applyFireworkBoostPreservingJump(Vector3dm velocity, Vector3dm look, int boosts, boolean isJump) {
        Vector3dm boosted = velocity.clone();
        for (int i = 0; i < boosts; i++) applyFireworkBoost(boosted, look);
        if (isJump) {
            boosted.setY(boosted.getY() + 0.04f * (1 - Math.pow(0.5, boosts)));
        }
        return boosted;
    }

    // When server has no glider, server can only realistically apply a single boost before its
    // own stopFallFlying triggers. In the toggle window we allow up to 2 to cover swap-in-flight
    // legit cases (1 just-fired + 1 still active), but never raw — otherwise N-rocket brute
    // forcing in the transition window grants ~0.85*N m/tick burst tolerance per tick.
    // Shared by the water engines, which also keep the glide flag (and thus the boost) underwater.
    public static int cappedFireworksForBoost(GrimPlayer player) {
        int rawMaxFireworks = player.fireworks.getMaxFireworksAppliedPossible();

        // Fix for 0.03 skipped ticks on < 1.18.2 with Elytra fireworks.
        // If the client skips a tick (because movement < 0.03), it processes the firework twice
        // before sending the next packet. We test an extra rocket to predict this accurately.
        if (rawMaxFireworks > 0 && player.isPointThree()) {
            rawMaxFireworks++;
        }

        boolean canGlide = player.isGliding || player.canGlide();
        boolean glidingRecent = player.uncertaintyHandler.lastGlidingChange.hasOccurredSince(4);

        if (!canGlide) {
            int cap = glidingRecent ? 2 : 1;
            return Math.min(cap, rawMaxFireworks);
        }
        return rawMaxFireworks;
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
        List<VectorData> results = new ArrayList<>();

        int maxFireworks = cappedFireworksForBoost(player);
        boolean hasFireworks = maxFireworks > 0 && (player.isGliding || player.wasGliding
                || player.uncertaintyHandler.lastGlidingChange.hasOccurredSince(4));
        boolean stuckSpeedActive = player.stuckSpeedMultiplier.getX() != 1.0
                || player.stuckSpeedMultiplier.getY() != 1.0
                || player.stuckSpeedMultiplier.getZ() != 1.0;
        // We must bruteforce Optifine ShitMath
        for (int shitmath = 0; shitmath <= 1; shitmath++, player.trigHandler.toggleShitMath()) {
            Vector3dm currentLook = ReachUtils.getLook(player, player.yaw, player.pitch);
            Vector3dm lastLook = hasFireworks ? ReachUtils.getLook(player, player.lastYaw, player.lastPitch) : null;
            Vector3dm[] fireworkLooks = hasFireworks ? new Vector3dm[]{currentLook, lastLook} : null;
            for (VectorData data : possibleVectors) {
                if (hasFireworks) {
                    for (int numRockets = 0; numRockets <= maxFireworks; numRockets++) {
                        if (numRockets == 0) {
                            processElytraResult(results, player, data, data.vector.clone(), currentLook, stuckSpeedActive);
                        } else {
                            applyRocketPermutations(numRockets, data.vector.clone(), fireworkLooks, results, player, data, currentLook, stuckSpeedActive);
                        }
                    }
                } else {
                    processElytraResult(results, player, data, data.vector.clone(), currentLook, stuckSpeedActive);
                }
            }
        }

        return results;
    }

    private void applyRocketPermutations(int remainingRockets, Vector3dm currentVelocity, Vector3dm[] fireworkLooks, List<VectorData> results, GrimPlayer player, VectorData data, Vector3dm currentLook, boolean stuckSpeedActive) {
        if (remainingRockets == 0) {
            processElytraResult(results, player, data, currentVelocity, currentLook, stuckSpeedActive);
            return;
        }
        for (Vector3dm look : fireworkLooks) {
            Vector3dm nextVelocity = currentVelocity.clone();
            applyFireworkBoost(nextVelocity, look);
            applyRocketPermutations(remainingRockets - 1, nextVelocity, fireworkLooks, results, player, data, currentLook, stuckSpeedActive);
        }
    }

    private void processElytraResult(List<VectorData> results, GrimPlayer player, VectorData data, Vector3dm inputVelocity, Vector3dm lookVector, boolean stuckSpeedActive) {
        VectorData result = data.returnNewModified(getElytraMovement(player, inputVelocity, lookVector), VectorData.VectorType.InputResult);

        if (player.uncertaintyHandler.shouldSimulateStuckSpeed) {
            // only simulate no stuck speed if player is leaving
            if (player.uncertaintyHandler.stuckSpeedMultiplierMask == 0 || !player.isForceStuckSpeed())
                addStuckSpeedResult(results, result, null);
            addStuckSpeedResult(results, result, player.stuckSpeedMultiplier);
            addPossibleStuckSpeedResults(player, results, result);
        } else {
            for (int applyStuckSpeed = 1; applyStuckSpeed >= 0; applyStuckSpeed--) {
                if (applyStuckSpeed == 0 && (player.isForceStuckSpeed() || !stuckSpeedActive)) break;

                addStuckSpeedResult(results, result, applyStuckSpeed != 0 ? player.stuckSpeedMultiplier : null);
            }
        }
    }

    private void addPossibleStuckSpeedResults(GrimPlayer player, List<VectorData> results, VectorData result) {
        int possibleStuckSpeedMultipliers = player.uncertaintyHandler.stuckSpeedMultiplierMask;
        for (IndexedVector3d stuckSpeedMultiplier : StuckSpeed.POSSIBILITIES) {
            if ((possibleStuckSpeedMultipliers & stuckSpeedMultiplier.getIndex()) != 0 && stuckSpeedMultiplier.getIndex() != player.stuckSpeedMultiplier.getIndex()) {
                addStuckSpeedResult(results, result, stuckSpeedMultiplier);
            }
        }
    }

    private void addStuckSpeedResult(List<VectorData> results, VectorData result, IndexedVector3d stuckSpeedMultiplier) {
        if (stuckSpeedMultiplier != null) {
            result = result.returnNewModified(result.vector.clone().multiply(stuckSpeedMultiplier), VectorData.VectorType.StuckMultiplier);
        }
        result.stuckSpeedMultiplier = stuckSpeedMultiplier == null ? StuckSpeed.NONE : stuckSpeedMultiplier;

        result = result.returnNewModified(result.vector.clone().multiply(0.99F, 0.98F, 0.99F), VectorData.VectorType.InputResult);
        results.add(result);
    }

    // Yes... you can jump while using an elytra as long as you are on the ground
    @Override
    public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
        new PredictionEngineNormal().addJumpsToPossibilities(player, existingVelocities);
    }
}
