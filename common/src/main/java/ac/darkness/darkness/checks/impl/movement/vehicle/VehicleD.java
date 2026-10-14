package ac.darkness.darkness.checks.impl.movement.vehicle;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.impl.verbose.VerboseCodecs;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "VehicleD", stableKey = "grim.vehicle.spoofed_jump", experimental = true, description = "Jumped in a vehicle that cannot jump")
public class VehicleD extends Check implements PacketReceiveListener {
    private static final Verbose V = Verbose.of("vehicle=[{entity}|null]");

    public VehicleD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE) {
            final EntityType vehicle = player.getVehicleType();

            if (!EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.ABSTRACT_HORSE) && !EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.ABSTRACT_NAUTILUS)) {
                if (flag(V.write(verbose()).bool(vehicle != null).uint(vehicle == null ? 0 : VerboseCodecs.entity(vehicle, player.getClientVersion()))) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
