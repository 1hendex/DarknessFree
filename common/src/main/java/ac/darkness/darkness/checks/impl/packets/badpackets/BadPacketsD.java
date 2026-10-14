package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsD", stableKey = "grim.badpackets.invalid_pitch", description = "Sent an invalid rotation pitch outside the -90 to 90 range")
public class BadPacketsD extends Check implements PacketReceiveListener {
    private static final Verbose V = Verbose.of("pitch={f32}");

    public BadPacketsD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.packetStateData.lastPacketWasTeleport) return;

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            final float pitch = new WrapperPlayClientPlayerFlying(event).getLocation().getPitch();
            if (pitch > 90 || pitch < -90) {
                // Ban.
                if (flag(V.write(verbose()).f32(pitch)) && shouldModifyPackets()) {
                    // prevent other checks from using an invalid pitch
                    if (player.pitch > 90) player.pitch = 90;
                    if (player.pitch < -90) player.pitch = -90;

                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
