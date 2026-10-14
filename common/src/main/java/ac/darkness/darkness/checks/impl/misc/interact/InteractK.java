package ac.darkness.darkness.checks.impl.misc.interact;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PreViaPacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectateEntity;

@CheckData(name = "InteractK", alternativeName = "SelfInteract", configName = "InteractK", stableKey = "grim.badpackets.self_hit", description = "Interacted with self")
public class InteractK extends Check implements PreViaPacketReceiveListener {
    public InteractK(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPreViaPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            onInteract(event, packet.getEntityId());
        }

        if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
            onInteract(event, packet.getEntityId());
        }

        if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
            WrapperPlayClientSpectateEntity packet = new WrapperPlayClientSpectateEntity(event);
            onInteract(event, packet.getEntityId());
        }
    }

    // TODO: should check for camera entity id instead of player entity id?
    private void onInteract(PacketReceiveEvent event, int entityId) {
        if (player.cameraEntity.isSelf() && entityId == player.entityID
                && flag() && shouldModifyPackets()) { // Instant ban
            event.setCancelled(true);
            player.onPacketCancel();
        }
    }
}
