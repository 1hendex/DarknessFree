package ac.darkness.darkness.utils.data.interpolation;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.collisions.datatypes.CollisionBox;
import ac.darkness.darkness.utils.collisions.datatypes.SimpleCollisionBox;
import ac.darkness.darkness.utils.data.packetentity.PacketEntity;
import ac.darkness.darkness.utils.nmsutil.GetBoundingBox;
import com.github.retrooper.packetevents.protocol.vector.positionpath.PositionPath;
import org.jetbrains.annotations.Nullable;

public interface EntityInterpolation {

    void begin(@Nullable PositionPath path, boolean relative, boolean hasPos, double packetX, double packetY, double packetZ, @Nullable Float yaw, @Nullable Float pitch, boolean positionSync, int transaction);

    void confirm(int transaction);

    void tick(boolean tickingReliably);

    void reset(SimpleCollisionBox position);

    SimpleCollisionBox position();

    default SimpleCollisionBox hitbox(GrimPlayer player, PacketEntity entity) {
        SimpleCollisionBox hitbox = position();
        GetBoundingBox.expandBoundingBoxByEntityDimensions(hitbox, player, entity);
        return hitbox;
    }

    default SimpleCollisionBox movementHitbox(GrimPlayer player, PacketEntity entity) {
        SimpleCollisionBox hitbox = position();
        GetBoundingBox.expandBoundingBoxByEntityDimensions(hitbox, player, entity);
        return hitbox;
    }

    default CollisionBox minimumHitbox(GrimPlayer player, PacketEntity entity) {
        return hitbox(player, entity);
    }

    default void initializeRotation(float yaw, float pitch) {
    }

}
