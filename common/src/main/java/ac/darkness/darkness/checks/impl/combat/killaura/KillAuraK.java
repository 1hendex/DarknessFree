package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "KillAuraK", alternativeName = "WallHit", configName = "KillAuraK", stableKey = "grim.combat.wall_hit", setback = 20, description = "Checks if the player attacked through walls")
public class KillAuraK extends Check {
    public KillAuraK(GrimPlayer player) {
        super(player);
    }
}
