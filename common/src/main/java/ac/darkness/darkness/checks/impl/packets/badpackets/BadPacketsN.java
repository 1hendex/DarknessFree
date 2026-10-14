package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "BadPacketsN", stableKey = "grim.badpackets.invalid_teleport", description = "Ignored or failed to accept a required server teleport")
public class BadPacketsN extends Check {
    public BadPacketsN(final GrimPlayer player) {
        super(player);
    }
}
