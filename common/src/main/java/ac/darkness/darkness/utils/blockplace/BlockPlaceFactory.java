package ac.darkness.darkness.utils.blockplace;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockPlace;

public interface BlockPlaceFactory {
    void applyBlockPlaceToWorld(GrimPlayer player, BlockPlace place);
}
