package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.impl.verbose.VerboseCodecs;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying.isFlying;

@CheckData(name = "ProtocolQ", alternativeName = "PacketOrderO", configName = "ProtocolQ", stableKey = "grim.packetorder.tick_end_order", description = "Sent packets after movement before the expected client tick end", experimental = true)
public class ProtocolQ extends Check implements PacketReceiveListener {
    private static final Verbose V = Verbose.of("type={packet}");

    public ProtocolQ(final GrimPlayer player) {
        super(player);
    }

    @Override
    public boolean isApplicable() {
        return player.supportsEndTick();
    }

    private boolean flying;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            flying = false;
        }

        if (isFlying(event.getPacketType()) && !player.packetStateData.lastPacketWasTeleport) {
            flying = true;
            return;
        }

        if (flying && !isAsync(event.getPacketType()) && event.getPacketType() != PacketType.Play.Client.VEHICLE_MOVE) {
            if (player.inVehicle() && event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction.Action action = new WrapperPlayClientEntityAction(event).getAction();
                if (action == WrapperPlayClientEntityAction.Action.START_SPRINTING || action == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                    return;
                }
            }

            int packetId = VerboseCodecs.packet(event.getPacketType(), player.getClientVersion());
            flag(V.write(verbose()).sint(packetId));
        }
    }
}
