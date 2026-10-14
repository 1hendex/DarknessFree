package ac.darkness.darkness.predictionengine.blockeffects;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.math.Vector3dm;

import java.util.List;

public interface BlockEffectsResolver {

    void applyEffectsFromBlocks(GrimPlayer player, Vector3dm clientVelocity, boolean onlyApplyVelocity, List<GrimPlayer.Movement> movements);

}
