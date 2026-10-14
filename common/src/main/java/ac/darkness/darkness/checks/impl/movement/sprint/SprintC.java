package ac.darkness.darkness.checks.impl.movement.sprint;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "SprintC", stableKey = "grim.sprint.using_item", description = "Sprinting while using an item", setback = 5, experimental = true)
public class SprintC extends Check implements PostPredictionListener {
    private boolean flaggedLastTick = false;

    public SprintC(GrimPlayer player) {
        super(player);
    }

    @Override
    public boolean isApplicable() {
        // https://bugs.mojang.com/browse/MC-152728
        return player.getClientVersion().isOlderThan(ClientVersion.V_1_14_2) || player.getClientVersion() == ClientVersion.V_1_21_4;
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.packetStateData.isSlowedByUsingItem()) {
            if (!player.wasTouchingWater || player.getClientVersion().isOlderThan(ClientVersion.V_1_13)) {
                flaggedLastTick = false;
                return;
            }

            if (player.isSprinting) {
                if (flaggedLastTick) flagWithSetback();
                flaggedLastTick = true;
            } else {
                reward();
                flaggedLastTick = false;
            }
        }
    }
}
