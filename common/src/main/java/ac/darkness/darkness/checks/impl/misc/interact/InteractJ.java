package ac.darkness.darkness.checks.impl.misc.interact;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.impl.verbose.VerboseCodecs;
import ac.darkness.darkness.checks.type.BlockBreakListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

@CheckData(name = "InteractJ", alternativeName = "InvalidBreak", configName = "InteractJ", stableKey = "grim.breaking.invalid_break", description = "Sent impossible block face id")
public class InteractJ extends Check implements BlockBreakListener {
    private static final Verbose V = Verbose.of("face={sint}, action={digging}");

    public InteractJ(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.faceId == 255 && blockBreak.action == DiggingAction.CANCELLED_DIGGING && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
            return;
        }

        if (blockBreak.faceId < 0 || blockBreak.faceId > 5) {
            // ban
            if (flag(V.write(verbose())
                    .sint(blockBreak.faceId)
                    .uint(VerboseCodecs.enumId(blockBreak.action))) && shouldModifyPackets()) {
                blockBreak.cancel();
            }
        }
    }
}
