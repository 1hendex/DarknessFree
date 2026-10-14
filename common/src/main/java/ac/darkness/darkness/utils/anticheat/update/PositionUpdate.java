package ac.darkness.darkness.utils.anticheat.update;

import ac.darkness.darkness.utils.data.SetBackData;
import ac.darkness.darkness.utils.data.TeleportData;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public final class PositionUpdate {
    private final Vector3d from, to;
    private final boolean onGround;
    private final SetBackData setback;
    private final TeleportData teleportData;
    private boolean isTeleport;
}
