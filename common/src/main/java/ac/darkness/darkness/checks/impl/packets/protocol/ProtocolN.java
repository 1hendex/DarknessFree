package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;

@CheckData(name = "ProtocolN", alternativeName = "PacketOrderM", configName = "ProtocolN", stableKey = "grim.packetorder.interact_use_order", description = "Sent use item and entity interaction packets in an invalid order", experimental = true)
public class ProtocolN extends Check implements PacketReceiveListener, PostPredictionListener {
    public ProtocolN(final GrimPlayer player) {
        super(player);
    }

    private int invalid;
    private boolean usingWithoutInteract, interacting;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (new WrapperPlayClientInteractEntity(event).getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                interacting = true;
                if (usingWithoutInteract) {
                    if (!player.canSkipTicks()) {
                        if (flag() && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                        }
                    } else {
                        invalid++;
                    }
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM
                || event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                && new WrapperPlayClientPlayerBlockPlacement(event).getFace() == BlockFace.OTHER) {
            if (!interacting) {
                usingWithoutInteract = true;
            }

            interacting = false;
        }

        if (!player.cameraEntity.isSelf() || isTickPacket(event.getPacketType())) {
            usingWithoutInteract = interacting = false;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3)) {
            for (; invalid >= 1; invalid--) {
                flag();
            }
        }

        invalid = 0;
    }
}
