package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PreViaPacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;

@CheckData(name = "ProtocolE", alternativeName = "PacketOrderD", configName = "ProtocolE", stableKey = "grim.packetorder.interact_hand_order", description = "Sent offhand entity interaction before the matching mainhand interaction", experimental = true)
public class ProtocolE extends Check implements PreViaPacketReceiveListener {
    private static final Verbose V = Verbose.of(
            "[Skipped Mainhand|requiredEntity={sint}, entity={sint}, requiredSneaking={bool}, sneaking={bool}]");

    public ProtocolE(final GrimPlayer player) {
        super(player);
    }

    @Override
    public boolean isApplicable() {
        return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
    }

    private boolean sentMainhand;
    private int requiredEntity;
    private boolean requiredSneaking;

    @Override
    public void onPreViaPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            InteractAction action = packet.getAction();
            if (action != InteractAction.ATTACK) {
                final boolean sneaking = packet.isSneaking().orElse(false);
                final int entity = packet.getEntityId();

                if (packet.getHand() == InteractionHand.OFF_HAND) {
                    if (action == InteractAction.INTERACT || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
                        if (!sentMainhand) {
                            if (flag(V.write(verbose())
                                    .bool(true) // skipped mainhand
                                    .sint(0)
                                    .sint(0)
                                    .bool(false)
                                    .bool(false)) && shouldModifyPackets()) {
                                event.setCancelled(true);
                                player.onPacketCancel();
                            }
                        }
                        sentMainhand = false;
                    }

                    if (action == InteractAction.INTERACT_AT) {
                        if (sneaking != requiredSneaking || entity != requiredEntity) {
                            if (flag(V.write(verbose())
                                    .bool(false) // mismatch
                                    .sint(requiredEntity)
                                    .sint(entity)
                                    .bool(requiredSneaking)
                                    .bool(sneaking)) && shouldModifyPackets()) {
                                event.setCancelled(true);
                                player.onPacketCancel();
                            }
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
