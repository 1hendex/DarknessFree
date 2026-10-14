package ac.darkness.darkness.checks.impl.combat.hitbox;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "HitBoxA", alternativeName = "Hitboxes", configName = "HitBoxA", stableKey = "grim.combat.hitboxes", description = "Checks for hitbox expansion")
public class HitBoxA extends Check {
    public HitBoxA(GrimPlayer player) {
        super(player);
    }
}
