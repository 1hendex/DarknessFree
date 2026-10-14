package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "KillAuraL", alternativeName = "EntityPierce", configName = "KillAuraL", stableKey = "grim.combat.entity_pierce", setback = 30, description = "Checks if the player attacked through entities")
public class KillAuraL extends Check {
    public KillAuraL(GrimPlayer player) {
        super(player);
    }
}
