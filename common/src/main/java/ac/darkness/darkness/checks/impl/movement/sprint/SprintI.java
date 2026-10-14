package ac.darkness.darkness.checks.impl.movement.sprint;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "SprintI", alternativeName = "SprintG", configName = "SprintI", stableKey = "grim.sprint.invalid_return", description = "Checks for invalid return sprint")
public class SprintI extends Check implements PacketReceiveListener {

    private int buffer1, buffer2;
    private int sprintToggleCounter = 0;
    private double buffer3 = 0.0;
    private long lastSprint;

    public SprintI(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            if (player.packetStateData.lastPacketWasTeleport ||
                    player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(3) ||
                    player.gamemode == GameMode.SPECTATOR || player.isFlying) {
                sprintToggleCounter = 0;
                return;
            }
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING)
                lastSprint = System.currentTimeMillis();
            final double deltaYaw = player.rotationProcessor.getDeltaYaw();
            final double deltaPitch = player.rotationProcessor.getDeltaPitch();
            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                long delta = time() - lastSprint;
                final boolean headMovement = deltaYaw > 2.5F && deltaPitch > 1.5F;
                if (delta < 80) {
                    if (player.actionManager.hasAttackedSince(500) && player.clickProcessor.getCps() < 3) {
                        if (++buffer1 > 3 && headMovement) {
                            flagAndAlert("Type: A | " + delta + " | " + buffer1);
                        }
                    }
                } else if (delta < 90) {
                    if (player.actionManager.hasAttackedSince(500) && player.clickProcessor.getCps() < 3) {
                        if (++buffer2 > 10 && headMovement) {
                            flagAndAlert("Type: B | " + delta + " | " + buffer2);
                        }
                    }
                } else {
                    buffer1 = 0;
                    buffer2 = 0;
                }
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING
                    || action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                sprintToggleCounter++;

                if (sprintToggleCounter > 1) {
                    if (player.actionManager.hasAttackedSince(1000)) {
                        if (buffer3++ > 3) {
                            flagAndAlert("Type: C | " + sprintToggleCounter + " | " + buffer3);
                        }
                    }
                } else {
                    buffer3 = Math.max(0, buffer3 - 0.25);
                }
            }
            return;
        }
        if (isTickPacket(event.getPacketType())) {
            sprintToggleCounter = 0;
        }
    }
}
