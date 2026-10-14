package ac.darkness.darkness.utils.data;

import ac.darkness.darkness.utils.math.Vector3dm;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BlockHitData extends HitData {
    Vector3i position;
    WrappedBlockState state;
    BlockFace closestDirection;

    public BlockHitData(Vector3i position, Vector3dm blockHitLocation, BlockFace closestDirection, WrappedBlockState state) {
        super(blockHitLocation);
        this.position = position;
        this.closestDirection = closestDirection;
        this.state = state;
    }

    public Vector3d getRelativeBlockHitLocation() {
        return new Vector3d(blockHitLocation.getX() - position.getX(), blockHitLocation.getY() - position.getY(), blockHitLocation.getZ() - position.getZ());
    }

    public Vector3i position() {
        return position;
    }

    public BlockFace closestDirection() {
        return closestDirection;
    }

    public WrappedBlockState state() {
        return state;
    }

    public boolean MojangEbanaty() {
        String block = state.getType().getName();
        return "tall_grass".equals(block) || "short_grass".equals(block) || "tall_seagrass".equals(block);
    }

    public boolean buging() {
        String str = state.getType().getName();
        return str.endsWith("_door") || (str.endsWith("_fence") || str.endsWith("_fence_gate")) || str.equals("chest")
                || str.equals("moving_piston") || str.endsWith("_trapdoor");
    }
}
