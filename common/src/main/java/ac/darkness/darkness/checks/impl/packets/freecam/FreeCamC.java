package ac.darkness.darkness.checks.impl.packets.freecam;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "FreeCamC", alternativeName = "BadPacketsQ", configName = "FreeCamC", stableKey = "grim.freecam.invalid_horse_jump", description = "Sent a horse jump packet with an invalid entity, action, or boost value")
public class FreeCamC extends Check implements PacketReceiveListener {

    public FreeCamC(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            int boost = wrapper.getJumpBoost();
            WrapperPlayClientEntityAction.Action action = wrapper.getAction();
            int entity = wrapper.getEntityId();
            // you are able to send negative jump boost, how and why!?
            if (Math.abs(boost) > 100
                    || entity != player.entityID
                    || action != WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE && boost != 0) {
                if (flag("boost=" + boost + ", action=" + action + ", entity=" + entity) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
