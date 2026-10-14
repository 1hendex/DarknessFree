package ac.darkness.darkness.checks.impl.movement.nofall;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "NoFallB", stableKey = "grim.groundspoof.fake", description = "Claimed to be on ground when predicted otherwise", setback = 10, decay = 0.01)
public class NoFallB extends Check implements PostPredictionListener {
    private static final Verbose V = Verbose.of("claimed {bool}");

    public NoFallB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        // Exemptions
        // Don't check players in spectator
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_8)
                && player.gamemode == GameMode.SPECTATOR || player.isInBerryBush)
            return;
        // And don't check this long list of ground exemptions
        if (player.exemptOnGround() || !predictionComplete.isChecked()) return;
        // Don't check if the player was on a ghost block
        if (player.getSetbackTeleportUtil().blockOffsets) return;
        // Viaversion sends wrong ground status... (doesn't matter but is annoying)
        if (player.packetStateData.lastPacketWasTeleport) return;

        if (player.clientClaimsLastOnGround == player.onGround) return;

        if (!player.packetStateData.packetPlayerOnGround) {
            flagWithSetback(V.write(verbose()).bool(player.clientClaimsLastOnGround));
        }
        player.checkManager.getNoFall().flipPlayerGroundStatus = true;
    }
}
