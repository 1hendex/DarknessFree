package ac.darkness.darkness.checks.impl.misc.interact;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "InteractN", alternativeName = "InvalidInteractTarget", configName = "InteractN", stableKey = "grim.badpackets.invalid_entity_target", description = "Interacted with non-existent entity", experimental = true)
public class InteractN extends Check {
    public InteractN(GrimPlayer player) {
        super(player);
    }
}
