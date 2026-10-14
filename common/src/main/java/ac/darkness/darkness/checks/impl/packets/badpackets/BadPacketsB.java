package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "BadPacketsB", stableKey = "grim.badpackets.ignored_rotation", description = "Ignored set rotation packet")
public class BadPacketsB extends Check {
    public BadPacketsB(final GrimPlayer player) {
        super(player);
    }
}
