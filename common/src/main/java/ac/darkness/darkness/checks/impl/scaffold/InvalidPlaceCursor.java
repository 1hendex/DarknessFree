package ac.darkness.darkness.checks.impl.scaffold;

import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.BlockPlaceCheck;
import ac.darkness.darkness.checks.type.BlockPlaceListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.util.Vector3f;

@CheckData(name = "InvalidPlaceCursor", stableKey = "grim.scaffolding.invalid_place_a", description = "Sent invalid cursor position")
public class InvalidPlaceCursor extends BlockPlaceCheck implements BlockPlaceListener {
    public InvalidPlaceCursor(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        Vector3f cursor = place.cursor;
        if (cursor == null) return;
        if (!Float.isFinite(cursor.x) || !Float.isFinite(cursor.y) || !Float.isFinite(cursor.z)) {
            if (flag() && shouldModifyPackets() && shouldCancel()) {
                place.resync();
            }
        }
    }
}
