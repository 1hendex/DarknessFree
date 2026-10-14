package ac.darkness.darkness.checks.impl.movement;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.config.ConfigManager;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "NoSlowA", alternativeName = "NoSlow", configName = "NoSlow", stableKey = "grim.movement.noslow", description = "Was not slowed while using an item", setback = 5)
public class NoSlowA extends Check implements PostPredictionListener {
    // The player sends that they switched items the next tick if they switch from an item that can be used
    // to another item that can be used.  What the fuck Mojang.  Affects 1.8 (and most likely 1.7) clients.
    public boolean didSlotChangeLastTick = false;
    public boolean flaggedLastTick = false;
    private double offsetToFlag;
    private double bestOffset = 1;
    private double buffer;

    public NoSlowA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        // If the player was using an item for certain, and their predicted velocity had a flipped item
        if (player.packetStateData.isSlowedByUsingItem()) {
            // 1.8 users are not slowed the first tick they use an item, strangely
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) && didSlotChangeLastTick) {
                didSlotChangeLastTick = false;
                buffer = 0;
            }

            if (player.stuckSpeedMultiplier.getX() < 0.99) {
                bestOffset = 1;
                return;
            }

            if (bestOffset > offsetToFlag) {
                buffer += bestOffset;
                if (buffer > 0.15) {
                    player.packetStateData.setSlowedByUsingItem(false);
                    GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(player.platformPlayer);
                    buffer = 0;
                    return;
                }
            } else {
                buffer = Math.max(0, buffer - 0.001);
            }

            if (bestOffset > offsetToFlag) {
                if (flaggedLastTick) {
                    flagWithSetback();
                }
                flaggedLastTick = true;
            } else {
                reward();
                flaggedLastTick = false;
            }
        }
        bestOffset = 1;
    }

    public void handlePredictionAnalysis(double offset) {
        bestOffset = Math.min(bestOffset, offset);
    }

    @Override
    public void onReload(@NotNull ConfigManager config) {
        offsetToFlag = config.getDoubleElse(getConfigName() + ".threshold", 0.001);
    }
}
