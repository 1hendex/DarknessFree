package ac.darkness.darkness.checks.impl.movement.prediction;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.event.events.CompletePredictionEvent;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@CheckData(name = "Interpolation (A)", alternativeName = "Interpolation", configName = "Interpolation", stableKey = "grim.prediction.simulation", description = "Checks for relies on interpolation to flag unnatural or abnormal motion", decay = 0.02)
public class OffsetHandler extends Check implements PostPredictionListener {
    private static final Verbose V = Verbose.of("{offset}");

    private static final AtomicInteger flags = new AtomicInteger(0);
    // Config
    private double setbackDecayMultiplier;
    private double threshold;
    private double immediateSetbackThreshold;
    private double maxAdvantage;
    private double maxCeiling;
    private double setbackViolationThreshold;
    // Current advantage gained
    private double advantageGained = 0;
    private static final CompletePredictionEvent.Channel COMPLETE_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(CompletePredictionEvent.class);

    public OffsetHandler(GrimPlayer player) {
        super(player);
    }

    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        double offset = predictionComplete.getOffset();

        if (COMPLETE_CHANNEL.fire(player, this, offset)) return;

        if ((offset >= threshold || offset >= immediateSetbackThreshold)) {
            advantageGained += offset;

            boolean isSetback = (advantageGained >= maxAdvantage || (offset >= immediateSetbackThreshold && violations >= 1));
            giveOffsetLenienceNextTick(offset);

            if (isSetback && !isNoSetbackPermission()) {
                player.getSetbackTeleportUtil().executeViolationSetback();
            }

            synchronized (flags) {
                int flagId = (flags.get() & 255) + 1; // 1-256 as possible values

                if (flag(V.write(verbose()).f64(offset), () -> humanFormattedOffset(offset) + " /gl " + flagId)) {
                    flags.incrementAndGet();
                    predictionComplete.setIdentifier(flagId);
                }
            }

            advantageGained = Math.min(advantageGained, maxCeiling);
        } else {
            advantageGained *= setbackDecayMultiplier;
        }

        removeOffsetLenience();
    }

    public static String humanFormattedOffset(double offset) {
        String humanFormattedOffset;
        if (offset < 0.001) { // 1.129E-3
            humanFormattedOffset = String.format("%.4E", offset);
            // Squeeze out an extra digit here by E-03 to E-3
            humanFormattedOffset = humanFormattedOffset.replace("E-0", "E-");
        } else {
            // 0.00112945678 -> .001129
            humanFormattedOffset = String.format("%6f", offset);
            // I like the leading zero, but removing it lets us add another digit to the end
            humanFormattedOffset = humanFormattedOffset.replace("0.", ".");
        }
        return humanFormattedOffset;
    }

    private void giveOffsetLenienceNextTick(double offset) {
        // Don't let players carry more than 1 offset into the next tick
        // (I was seeing cheats try to carry 1,000,000,000 offset into the next tick!)
        //
        // This value so that setting back with high ping doesn't allow players to gather high client velocity
        double minimizedOffset = Math.min(offset, 1);

        // Normalize offsets
        player.uncertaintyHandler.lastHorizontalOffset = minimizedOffset;
        player.uncertaintyHandler.lastVerticalOffset = minimizedOffset;
    }

    private void removeOffsetLenience() {
        player.uncertaintyHandler.lastHorizontalOffset = 0;
        player.uncertaintyHandler.lastVerticalOffset = 0;
    }

    @Override
    public void onReload(@NotNull ConfigManager config) {
        setbackDecayMultiplier = config.getDoubleElse("Interpolation.setback-decay-multiplier", config.getDoubleElse("Simulation.setback-decay-multiplier", 0.999));
        threshold = config.getDoubleElse("Interpolation.threshold", config.getDoubleElse("Simulation.threshold", 0.001));
        immediateSetbackThreshold = config.getDoubleElse("Interpolation.immediate-setback-threshold", config.getDoubleElse("Simulation.immediate-setback-threshold", 0.1));
        maxAdvantage = config.getDoubleElse("Interpolation.max-advantage", config.getDoubleElse("Simulation.max-advantage", 1));
        maxCeiling = config.getDoubleElse("Interpolation.max-ceiling", config.getDoubleElse("Simulation.max-ceiling", 4));
        setbackViolationThreshold = config.getDoubleElse("Interpolation.setback-violation-threshold", config.getDoubleElse("Simulation.setback-violation-threshold", 1));
        if (maxAdvantage == -1) maxAdvantage = Double.MAX_VALUE;
        if (immediateSetbackThreshold == -1) immediateSetbackThreshold = Double.MAX_VALUE;
    }

    public boolean doesOffsetFlag(double offset) {
        return offset >= threshold;
    }
}
