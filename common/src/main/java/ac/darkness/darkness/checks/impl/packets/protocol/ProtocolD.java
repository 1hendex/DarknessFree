package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "ProtocolD", alternativeName = "PacketOrderD", configName = "ProtocolD", stableKey = "grim.protocol.interact_hand", description = "Checks for invalid hand interaction")
public class ProtocolD extends Check implements PacketReceiveListener {
    public ProtocolD(final GrimPlayer player) {
        super(player);
    }

    private boolean sentMainhand;
    private int requiredEntity;
    private boolean requiredSneaking;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();
            if (action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final boolean sneaking = packet.isSneaking().orElse(false);
                final int entity = packet.getEntityId();

                if (packet.getHand() == InteractionHand.OFF_HAND) {
                    if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
                        if (!sentMainhand) {
                            if (flag("Skipped Mainhand") && shouldModifyPackets()) {
                                event.setCancelled(true);
                                player.onPacketCancel();
                            }
                        }
                        sentMainhand = false;
                    } else if (sneaking != requiredSneaking || entity != requiredEntity) {
                        String verbose = "requiredEntity=" + requiredEntity + ", entity=" + entity
                                + ", requiredSneaking=" + requiredSneaking + ", sneaking=" + sneaking;
                        if (flag(verbose) && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                        }
                    }
                } else {
                    requiredEntity = entity;
                    requiredSneaking = sneaking;
                    sentMainhand = true;
                }
            }
        }

        if (isTickPacket(event.getPacketType())) {
            sentMainhand = false;
        }
    }
}
