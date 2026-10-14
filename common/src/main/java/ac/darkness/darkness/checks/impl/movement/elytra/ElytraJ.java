package ac.darkness.darkness.checks.impl.movement.elytra;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "ElytraJ", alternativeName = "ElytraB", configName = "ElytraJ", stableKey = "grim.elytra.invalid_acceleration", description = "Checks for invalid elytra accelerations")
public class ElytraJ extends Check implements PacketReceiveListener {

    private double buffer = 0;

    public ElytraJ(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (player.isInWeb || player.isInBerryBush || player.wasTouchingWater ||
                player.wasSwimming || player.packetStateData.lastPacketWasOnePointSeventeenDuplicate ||
                player.packetStateData.lastPacketWasTeleport || player.getVelocity() || player.hasInventoryOpen) {
            return;
        }
        if (player.getTransactionPing() > 500 || player.getTPS() < 18.5) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION && new WrapperPlayClientEntityAction(event)
                .getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA) {
            final double accelY = player.accelY;
            final double accelXZ = player.accelXZ;
            if (accelY <= 0.0 && accelXZ <= 0.0) {
                if (buffer++ > 5) {
                    flagAndAlert("accel=" + accelXZ + " | " + accelY);
                }
            } else {
                buffer = Math.max(0, buffer - 0.25);
                reward();
            }
        }
    }
}
