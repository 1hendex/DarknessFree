package ac.darkness.darkness.checks.impl.misc.interact;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "InteractL", alternativeName = "InvalidInteractCursor", configName = "InteractL", stableKey = "grim.badpackets.invalid_interact_vector", description = "Sent an entity interaction vector outside the target player's hitbox")
public class InteractL extends Check implements PacketReceiveListener {
    private static final Verbose V = Verbose.of("{f64:%.5f}/{f64:%.5f}/{f64:%.5f}");

    private final double maxHorizontalDisplacement;
    private final double minVerticalDisplacement;
    private final double maxVerticalDisplacement;

    public InteractL(final GrimPlayer player) {
        super(player);
        // pre-1.9 expands hitboxes by 0.1 on all sides; this is not lenience, it is vanilla.
        double expansion = player.getClientVersion().isOlderThan(ClientVersion.V_1_9) ? 0.1f : 0;
        maxHorizontalDisplacement = 0.3001 + expansion;
        minVerticalDisplacement = -0.0001 - expansion;
        maxVerticalDisplacement = 1.8001 + expansion;
    }

    @Override
    public boolean isApplicable() {
        return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Client.INTERACT_ENTITY)) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            // Only INTERACT_AT actually has an interaction vector
            if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;
            Vector3d targetVector = wrapper.getLocation();
            if (targetVector == null) return; // shouldn't ever happen, but whatever

            if (!Double.isFinite(targetVector.x) || !Double.isFinite(targetVector.y) || !Double.isFinite(targetVector.z)) {
                flag(V.write(verbose()).f64(targetVector.x).f64(targetVector.y).f64(targetVector.z));
                return;
            }

            final PacketEntity packetEntity = player.compensatedEntities.getEntity(wrapper.getEntityId());
            // Don't continue if the compensated entity hasn't been resolved
            if (packetEntity == null) {
                return;
            }

            // Make sure our target entity is actually a player (Player NPCs work too)
            if (!EntityTypes.PLAYER.equals(packetEntity.getType())) {
                return;
            }

            final double x = targetVector.getX();
            final double y = targetVector.getY();
            final double z = targetVector.getZ();

            final double scale = packetEntity.getAttributeValue(Attributes.SCALE);

            if (Math.abs(x) > maxHorizontalDisplacement * scale
                    || Math.abs(z) > maxHorizontalDisplacement * scale
                    || y < minVerticalDisplacement * scale
                    || y > maxVerticalDisplacement * scale) {
                flag(V.write(verbose()).f64(x).f64(y).f64(z));
            }
        }
    }
}
