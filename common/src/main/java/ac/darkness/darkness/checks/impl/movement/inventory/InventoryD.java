package ac.darkness.darkness.checks.impl.movement.inventory;

import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.InventoryCheck;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import ac.darkness.darkness.utils.data.VectorData;
import ac.darkness.darkness.utils.data.VectorData.MoveVectorData;
import ac.darkness.darkness.utils.data.VehicleData;

import java.util.StringJoiner;

@CheckData(name = "InventoryD", configName = "InventoryD", stableKey = "grim.inventory.move", setback = 1, decay = 0.25, description = "Moved or jumped while inventory is open")
public class InventoryD extends InventoryCheck implements PostPredictionListener {
    private int horseJumpVerbose;

    public InventoryD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked() ||
                predictionComplete.getData().isTeleport() ||
                player.getSetbackTeleportUtil().blockOffsets ||
                player.packetStateData.lastPacketWasTeleport ||
                player.packetStateData.isSlowedByUsingItem() ||
                System.currentTimeMillis() - player.lastBlockPlaceUseItem < 50L) {
            return;
        }

        if (player.hasInventoryOpen) {
            boolean inVehicle = player.inVehicle();
            boolean isJumping, isMoving;

            if (inVehicle) {
                VehicleData vehicle = player.vehicleData;

                // Will flag once if player open anything with pressed space bar
                isJumping = vehicle.nextHorseJump > 0 && horseJumpVerbose++ >= 1;
                isMoving = vehicle.nextVehicleForward != 0 || vehicle.nextVehicleHorizontal != 0;
            } else {
                MoveVectorData move = findMovement(player.predictedVelocity);

                isJumping = player.predictedVelocity.isJump();
                isMoving = move != null && (move.x != 0 || move.z != 0);
            }

            if (!isMoving && !isJumping) {
                reward();
                return;
            }

            if (flag()) {
                if (!isNoSetbackPermission())
                    closeInventory();

                StringJoiner joiner = new StringJoiner(" ");

                if (isMoving) joiner.add("moving");
                if (isJumping) joiner.add("jumping");
                if (inVehicle) joiner.add("inVehicle");

                alert(joiner.toString());
            }
        } else {
            horseJumpVerbose = 0;
        }
    }

    private MoveVectorData findMovement(VectorData vectorData) {
        if (vectorData instanceof MoveVectorData) {
            return (MoveVectorData) vectorData;
        }

        while (vectorData != null) {
            vectorData = vectorData.lastVector;
            if (vectorData instanceof MoveVectorData) {
                return (MoveVectorData) vectorData;
            }
        }

        return null;
    }
}
